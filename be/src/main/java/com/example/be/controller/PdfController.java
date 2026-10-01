package com.contractexposed.backend.controller;

import com.contractexposed.backend.entity.Contract;
import com.contractexposed.backend.entity.RiskFinding;
import com.contractexposed.backend.exception.ContractNotFoundException;
import com.contractexposed.backend.exception.FileProcessingException;
import com.contractexposed.backend.repository.ContractRepository;
import com.contractexposed.backend.repository.RiskFindingRepository;
import com.contractexposed.backend.service.FileStorageService;
import com.contractexposed.backend.service.PdfHighlightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

/**
 * GET /api/pdf/{contractId}/original    – serve the raw uploaded file
 * GET /api/pdf/{contractId}/highlighted – serve annotated PDF (highlights)
 */
@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
@Slf4j
public class PdfController {

    private final ContractRepository   contractRepo;
    private final RiskFindingRepository findingRepo;
    private final FileStorageService   storageService;
    private final PdfHighlightService  highlightService;

    // ── Original file ─────────────────────────────────────────

    @GetMapping("/{contractId}/original")
    public ResponseEntity<Resource> original(@PathVariable Long contractId) {
        Contract contract = contractRepo.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));

        Path filePath = storageService.resolve(contract.getFileName());
        try {
            UrlResource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new FileProcessingException("File not found on disk.");
            }
            MediaType mediaType = resolveMediaType(contract.getFileType());
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + contract.getOriginalFileName() + "\"")
                    .body(resource);
        } catch (MalformedURLException ex) {
            throw new FileProcessingException("Invalid file path.", ex);
        }
    }

    // ── Highlighted PDF ───────────────────────────────────────

    @GetMapping("/{contractId}/highlighted")
    public ResponseEntity<Resource> highlighted(@PathVariable Long contractId) {
        Contract contract = contractRepo.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));

        if (contract.getFileType() != Contract.FileType.PDF) {
            throw new FileProcessingException("Highlighting is only available for PDF files.");
        }
        if (contract.getAnalysisReport() == null) {
            throw new FileProcessingException("Analysis not yet complete.");
        }

        List<RiskFinding> findings = findingRepo
                .findByReportIdOrderBySortOrderAscSeverityScoreDesc(
                        contract.getAnalysisReport().getId());

        byte[] pdfBytes = highlightService.generateHighlightedPdf(contract.getFileName(), findings);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"highlighted_" + contract.getOriginalFileName() + "\"")
                .body(new ByteArrayResource(pdfBytes));
    }

    // ── Helper ────────────────────────────────────────────────

    private MediaType resolveMediaType(Contract.FileType type) {
        return switch (type) {
            case PDF   -> MediaType.APPLICATION_PDF;
            case DOCX, DOC -> MediaType.APPLICATION_OCTET_STREAM;
            case IMAGE -> MediaType.IMAGE_JPEG;
        };
    }
}
