package com.contractexposed.backend.service;

import com.contractexposed.backend.entity.*;
import com.contractexposed.backend.exception.FileProcessingException;
import com.contractexposed.backend.repository.AnalysisReportRepository;
import com.contractexposed.backend.repository.ContractRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Calls the Google Gemini API (generateContent), parses the JSON response,
 * persists findings + checklist, and scores the report.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ContractAnalysisService {

    private final ContractRepository       contractRepo;
    private final AnalysisReportRepository reportRepo;
    private final RiskScoringEngine        scoringEngine;
    private final ObjectMapper             objectMapper;

    @Value("${app.gemini.api-key}")
    private String geminiApiKey;

    @Value("${app.gemini.model:gemini-2.5-flash-lite}")
    private String model;

    @Value("${app.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    // ─────────────────────────────────────────────────────────
    //  Main entry point — called asynchronously by the pipeline
    // ─────────────────────────────────────────────────────────

    @Transactional
    public void analyze(Long contractId) {
        Contract contract = contractRepo.findByIdWithReport(contractId)
                .orElseThrow(() -> new FileProcessingException("Contract not found: " + contractId));

        if (contract.getExtractedText() == null || contract.getExtractedText().isBlank()) {
            markFailed(contract, "No text could be extracted from this file.");
            return;
        }

        contract.setStatus(Contract.ContractStatus.ANALYZING);
        contractRepo.save(contract);

        long startMs = System.currentTimeMillis();
        try {
            String aiJson    = callGemini(contract.getExtractedText());
            long   durationMs = System.currentTimeMillis() - startMs;
            AnalysisReport report = buildReport(contract, aiJson, durationMs);
            scoringEngine.score(report);
            reportRepo.save(report);

            contract.setStatus(Contract.ContractStatus.ANALYZED);
            contract.setAnalysisReport(report);
            contractRepo.save(contract);
            log.info("Analysis complete for contract {} — score {}", contractId, report.getOverallRiskScore());

        } catch (Exception ex) {
            log.error("Analysis failed for contract {}: {}", contractId, ex.getMessage(), ex);
            markFailed(contract, ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Gemini call  (POST /models/{model}:generateContent?key=...)
    //  Retries up to 3 times on 503 Service Unavailable
    // ─────────────────────────────────────────────────────────

    private static final int    MAX_RETRIES    = 5;
    private static final long   RETRY_DELAY_MS = 8_000;

    private String callGemini(String contractText) {
        String userPrompt = buildPrompt(contractText);

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("role", "user", "parts", List.of(
                    Map.of("text", SYSTEM_PROMPT + "\n\n" + userPrompt)
                ))
            ),
            "generationConfig", Map.of(
                "temperature",      0.2,
                "responseMimeType", "application/json"
            )
        );

        RestClient client = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                String response = client.post()
                        .uri("/models/{model}:generateContent?key={key}", model, geminiApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestBody)
                        .retrieve()
                        .body(String.class);

                JsonNode root = objectMapper.readTree(response);
                return root.path("candidates").get(0)
                           .path("content").path("parts").get(0)
                           .path("text").asText();

            } catch (HttpServerErrorException e) {
                lastException = e;
                log.warn("Gemini attempt {}/{} failed with {}: {}",
                        attempt, MAX_RETRIES, e.getStatusCode(), e.getResponseBodyAsString());
                if (attempt < MAX_RETRIES) {
                    try { Thread.sleep(RETRY_DELAY_MS * attempt); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (Exception e) {
                throw new FileProcessingException("Failed to call Gemini API: " + e.getMessage(), e);
            }
        }
        throw new FileProcessingException(
                "Gemini API unavailable after " + MAX_RETRIES + " attempts: " + lastException.getMessage(),
                lastException);
    }

    // ─────────────────────────────────────────────────────────
    //  Parse AI JSON → AnalysisReport
    // ─────────────────────────────────────────────────────────

    private AnalysisReport buildReport(Contract contract, String aiJson, long durationMs) {
        try {
            // Gemini sometimes wraps JSON in ```json ... ``` — strip it
            String clean = aiJson.trim();
            if (clean.startsWith("```")) {
                clean = clean.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").trim();
            }

            JsonNode root = objectMapper.readTree(clean);

            AnalysisReport report = AnalysisReport.builder()
                    .contract(contract)
                    .summary(root.path("summary").asText("Không có tóm tắt."))
                    .aiRawResponse(aiJson)
                    .modelUsed(model)
                    .analysisDurationMs(durationMs)
                    .build();

            // ── Findings ─────────────────────────────────────
            List<RiskFinding> findings = new ArrayList<>();
            JsonNode findingsNode = root.path("findings");
            if (findingsNode.isArray()) {
                int order = 0;
                for (JsonNode fn : findingsNode) {
                    RiskFinding.Severity severity = parseSeverity(fn.path("severity").asText("MEDIUM"));
                    RiskFinding.RiskType riskType  = parseRiskType(fn.path("riskType").asText("OTHER"));

                    RiskFinding finding = RiskFinding.builder()
                            .report(report)
                            .clauseTitle(fn.path("clauseTitle").asText("Điều khoản không rõ"))
                            .originalText(fn.path("originalText").asText(""))
                            .riskType(riskType)
                            .severity(severity)
                            .severityScore(fn.path("severityScore").asInt(severity.getDefaultScore()))
                            .explanation(fn.path("explanation").asText(""))
                            .recommendation(fn.path("recommendation").asText(""))
                            .pageNumber(fn.path("pageNumber").isNull() ? null : fn.path("pageNumber").asInt())
                            .highlightColor(severity.getColor())
                            .sortOrder(order++)
                            .build();
                    findings.add(finding);
                }
            }
            report.setFindings(findings);

            // ── Checklist ─────────────────────────────────────
            List<NegotiationChecklistItem> checklist = new ArrayList<>();
            JsonNode checklistNode = root.path("negotiationChecklist");
            if (checklistNode.isArray()) {
                int order = 0;
                for (JsonNode item : checklistNode) {
                    NegotiationChecklistItem.Priority priority =
                            parsePriority(item.path("priority").asText("MEDIUM"));
                    checklist.add(NegotiationChecklistItem.builder()
                            .report(report)
                            .itemText(item.path("itemText").asText(""))
                            .priority(priority)
                            .category(item.path("category").asText(null))
                            .sortOrder(order++)
                            .build());
                }
            }
            report.setChecklistItems(checklist);

            report.setTokensUsed(root.path("_meta").path("tokensUsed").asInt(0));

            return report;

        } catch (Exception ex) {
            throw new FileProcessingException("Failed to parse AI analysis JSON: " + ex.getMessage(), ex);
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────

    private void markFailed(Contract contract, String reason) {
        contract.setStatus(Contract.ContractStatus.FAILED);
        contract.setErrorMessage(reason);
        contractRepo.save(contract);
    }

    private String buildPrompt(String contractText) {
        int maxChars = 12_000;
        String text  = contractText.length() > maxChars
                ? contractText.substring(0, maxChars) + "\n[... văn bản bị cắt bớt ...]"
                : contractText;
        return "Phân tích hợp đồng sau và trả lời theo định dạng JSON đã chỉ định:\n\n" + text;
    }

    private RiskFinding.Severity parseSeverity(String s) {
        try { return RiskFinding.Severity.valueOf(s.toUpperCase()); }
        catch (Exception e) { return RiskFinding.Severity.MEDIUM; }
    }

    private RiskFinding.RiskType parseRiskType(String s) {
        try { return RiskFinding.RiskType.valueOf(s.toUpperCase()); }
        catch (Exception e) { return RiskFinding.RiskType.OTHER; }
    }

    private NegotiationChecklistItem.Priority parsePriority(String s) {
        try { return NegotiationChecklistItem.Priority.valueOf(s.toUpperCase()); }
        catch (Exception e) { return NegotiationChecklistItem.Priority.MEDIUM; }
    }

    // ─────────────────────────────────────────────────────────
    //  System prompt (Vietnamese-aware)
    // ─────────────────────────────────────────────────────────

    private static final String SYSTEM_PROMPT = """
        Bạn là chuyên gia phân tích hợp đồng pháp lý tại Việt Nam. Nhiệm vụ:
        Phân tích văn bản hợp đồng → phát hiện các điều khoản bất lợi, rủi ro cho bên thuê/người dùng.

        Trả về JSON THUẦN TÚY (không có markdown) theo cấu trúc:
        {
          "summary": "Tóm tắt rủi ro tổng thể bằng tiếng Việt (3-5 câu)",
          "findings": [
            {
              "clauseTitle": "Tên điều khoản",
              "originalText": "Đoạn văn gốc từ hợp đồng",
              "riskType": "FINANCIAL|LEGAL|TERMINATION|PRIVACY|PENALTY|RENEWAL|DISPUTE|OTHER",
              "severity": "LOW|MEDIUM|HIGH|CRITICAL",
              "severityScore": 0-100,
              "explanation": "Giải thích tại sao điều khoản này bất lợi (tiếng Việt)",
              "recommendation": "Đề xuất sửa đổi hoặc yêu cầu đàm phán (tiếng Việt)",
              "pageNumber": null
            }
          ],
          "negotiationChecklist": [
            {
              "itemText": "Việc cần làm trước khi ký (tiếng Việt)",
              "priority": "LOW|MEDIUM|HIGH",
              "category": "Tài chính|Pháp lý|Chấm dứt hợp đồng|Khác"
            }
          ],
          "_meta": { "tokensUsed": 0 }
        }

        Quy tắc:
        - Chỉ trả về JSON, không có giải thích ngoài JSON
        - Ưu tiên điều khoản tài chính, phạt, chấm dứt, tự động gia hạn
        - Giải thích bằng tiếng Việt đơn giản, dễ hiểu
        - Tối thiểu 3, tối đa 15 findings
        - Tối thiểu 3, tối đa 10 checklist items
        """;
}
