package com.contractexposed.backend.dto;

import com.contractexposed.backend.entity.AnalysisReport;
import com.contractexposed.backend.entity.Contract;
import com.contractexposed.backend.entity.RiskFinding;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Full analysis report returned after AI processing completes.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalysisReportDto {

    // ── Contract metadata ────────────────────────────────────
    private Long                    contractId;
    private String                  originalFileName;
    private String                  fileType;
    private Long                    fileSize;
    private Contract.ContractStatus contractStatus;

    // ── Report core ──────────────────────────────────────────
    private Long                    reportId;
    private Integer                 overallRiskScore;
    private AnalysisReport.RiskLevel riskLevel;
    private String                  summary;
    private String                  modelUsed;
    private Integer                 tokensUsed;
    private Long                    analysisDurationMs;
    private LocalDateTime           analyzedAt;

    // ── Finding breakdown ────────────────────────────────────
    private List<RiskFindingDto>            findings;
    private Map<String, Long>               findingsBySeverity;
    private Map<String, Long>               findingsByType;
    private List<NegotiationChecklistItemDto> checklistItems;

    // ── Stats ────────────────────────────────────────────────
    private long totalFindings;
    private long criticalCount;
    private long highCount;
    private long mediumCount;
    private long lowCount;

    public static AnalysisReportDto from(Contract contract, AnalysisReport report) {
        List<RiskFindingDto> findingDtos = report.getFindings().stream()
                .map(RiskFindingDto::from)
                .collect(Collectors.toList());

        List<NegotiationChecklistItemDto> checklistDtos = report.getChecklistItems().stream()
                .map(NegotiationChecklistItemDto::from)
                .collect(Collectors.toList());

        Map<String, Long> bySeverity = report.getFindings().stream()
                .collect(Collectors.groupingBy(
                        f -> f.getSeverity().name(),
                        Collectors.counting()));

        Map<String, Long> byType = report.getFindings().stream()
                .collect(Collectors.groupingBy(
                        f -> f.getRiskType().name(),
                        Collectors.counting()));

        long critical = bySeverity.getOrDefault(RiskFinding.Severity.CRITICAL.name(), 0L);
        long high     = bySeverity.getOrDefault(RiskFinding.Severity.HIGH.name(),     0L);
        long medium   = bySeverity.getOrDefault(RiskFinding.Severity.MEDIUM.name(),   0L);
        long low      = bySeverity.getOrDefault(RiskFinding.Severity.LOW.name(),      0L);

        return AnalysisReportDto.builder()
                .contractId(contract.getId())
                .originalFileName(contract.getOriginalFileName())
                .fileType(contract.getFileType().name())
                .fileSize(contract.getFileSize())
                .contractStatus(contract.getStatus())
                .reportId(report.getId())
                .overallRiskScore(report.getOverallRiskScore())
                .riskLevel(report.getRiskLevel())
                .summary(report.getSummary())
                .modelUsed(report.getModelUsed())
                .tokensUsed(report.getTokensUsed())
                .analysisDurationMs(report.getAnalysisDurationMs())
                .analyzedAt(report.getCreatedAt())
                .findings(findingDtos)
                .findingsBySeverity(bySeverity)
                .findingsByType(byType)
                .checklistItems(checklistDtos)
                .totalFindings(report.getFindings().size())
                .criticalCount(critical)
                .highCount(high)
                .mediumCount(medium)
                .lowCount(low)
                .build();
    }
}
