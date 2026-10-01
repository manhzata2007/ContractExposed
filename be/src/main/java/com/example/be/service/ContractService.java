package com.contractexposed.backend.service;

import com.contractexposed.backend.dto.*;
import com.contractexposed.backend.entity.Contract;
import com.contractexposed.backend.entity.NegotiationChecklistItem;
import com.contractexposed.backend.exception.ContractNotFoundException;
import com.contractexposed.backend.exception.FileProcessingException;
import com.contractexposed.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrates the full upload → extract → analyse pipeline.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository            contractRepo;
    private final AnalysisReportRepository      reportRepo;
    private final NegotiationChecklistRepository checklistRepo;
    private final FileStorageService            storageService;
    private final TextExtractionService         textExtractor;
    private final ContractAnalysisService       analysisService;

    // ── Upload ────────────────────────────────────────────────

    @Transactional
    public ContractUploadResponse upload(MultipartFile file, String clientIp) {
        String storedName = storageService.store(file);
        Contract contract = Contract.builder()
                .originalFileName(file.getOriginalFilename())
                .fileName(storedName)
                .filePath(storageService.resolve(storedName).toString())
                .fileType(storageService.detectFileType(file))
                .fileSize(file.getSize())
                .uploadIp(clientIp)
                .build();
        contract = contractRepo.save(contract);
        log.info("Uploaded contract id={} name={}", contract.getId(), contract.getOriginalFileName());

        // kick off async pipeline
        processAsync(contract.getId());

        return ContractUploadResponse.from(contract);
    }

    // ── Async pipeline ────────────────────────────────────────

    @Async
    public void processAsync(Long contractId) {
        extractText(contractId);
        analysisService.analyze(contractId);
    }

    @Transactional
    public void extractText(Long contractId) {
        Contract contract = contractRepo.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));

        contract.setStatus(Contract.ContractStatus.EXTRACTING);
        contractRepo.save(contract);

        try {
            Path filePath = storageService.resolve(contract.getFileName());
            String text   = textExtractor.extract(filePath, contract.getFileType());
            contract.setExtractedText(text);
            contract.setStatus(Contract.ContractStatus.EXTRACTED);
            contractRepo.save(contract);
            log.info("Extracted {} chars from contract {}", text.length(), contractId);
        } catch (Exception ex) {
            contract.setStatus(Contract.ContractStatus.FAILED);
            contract.setErrorMessage("Text extraction failed: " + ex.getMessage());
            contractRepo.save(contract);
            throw new FileProcessingException("Text extraction failed", ex);
        }
    }

    // ── Status polling ────────────────────────────────────────

    @Transactional(readOnly = true)
    public ContractStatusDto getStatus(Long contractId) {
        Contract contract = contractRepo.findByIdWithReport(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));
        return ContractStatusDto.from(contract);
    }

    // ── Full report ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public AnalysisReportDto getReport(Long contractId) {
        Contract contract = contractRepo.findByIdWithReport(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));

        if (contract.getAnalysisReport() == null) {
            throw new FileProcessingException("Analysis report not yet available for contract " + contractId);
        }

        // Fetch findings and checklist in two separate queries to avoid
        // MultipleBagFetchException when JOIN FETCHing two List collections at once.
        var report = reportRepo.findByContractIdWithFindings(contractId)
                .orElseThrow(() -> new FileProcessingException("Report details not found."));
        // Second query populates checklistItems into the same managed entity
        reportRepo.findByContractIdWithChecklist(contractId);

        return AnalysisReportDto.from(contract, report);
    }

    // ── List ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ContractSummaryDto> listContracts(int page, int size) {
        return contractRepo.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(ContractSummaryDto::from);
    }

    // ── Delete ────────────────────────────────────────────────

    @Transactional
    public void delete(Long contractId) {
        Contract contract = contractRepo.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));
        storageService.delete(contract.getFileName());
        contractRepo.delete(contract);
        log.info("Deleted contract {}", contractId);
    }

    // ── Checklist toggle ──────────────────────────────────────

    @Transactional
    public NegotiationChecklistItemDto toggleChecklistItem(Long itemId, Boolean isCompleted) {
        NegotiationChecklistItem item = checklistRepo.findById(itemId)
                .orElseThrow(() -> new FileProcessingException("Checklist item not found: " + itemId));
        item.setIsCompleted(isCompleted);
        return NegotiationChecklistItemDto.from(checklistRepo.save(item));
    }

    // ── Dashboard stats ───────────────────────────────────────

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        long total    = contractRepo.count();
        long analyzed = contractRepo.countByStatus(Contract.ContractStatus.ANALYZED);
        long pending  = contractRepo.countByStatus(Contract.ContractStatus.UPLOADED)
                      + contractRepo.countByStatus(Contract.ContractStatus.EXTRACTING)
                      + contractRepo.countByStatus(Contract.ContractStatus.EXTRACTED)
                      + contractRepo.countByStatus(Contract.ContractStatus.ANALYZING);
        long failed   = contractRepo.countByStatus(Contract.ContractStatus.FAILED);

        // Risk level counts from reports
        List<Object[]> riskCounts = reportRepo.findAll().stream()
                .collect(Collectors.groupingBy(r -> r.getRiskLevel().name(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .collect(Collectors.toList());

        long critical = riskCounts.stream()
                .filter(a -> "CRITICAL".equals(a[0])).mapToLong(a -> (long) a[1]).sum();
        long high     = riskCounts.stream()
                .filter(a -> "HIGH".equals(a[0])).mapToLong(a -> (long) a[1]).sum();
        long medium   = riskCounts.stream()
                .filter(a -> "MEDIUM".equals(a[0])).mapToLong(a -> (long) a[1]).sum();
        long low      = riskCounts.stream()
                .filter(a -> "LOW".equals(a[0])).mapToLong(a -> (long) a[1]).sum();

        return DashboardStatsDto.builder()
                .totalContracts(total)
                .analyzedContracts(analyzed)
                .pendingContracts(pending)
                .failedContracts(failed)
                .criticalRiskContracts(critical)
                .highRiskContracts(high)
                .mediumRiskContracts(medium)
                .lowRiskContracts(low)
                .build();
    }
}
