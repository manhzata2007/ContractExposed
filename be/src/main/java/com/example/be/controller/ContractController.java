package com.contractexposed.backend.controller;

import com.contractexposed.backend.dto.*;
import com.contractexposed.backend.service.ContractService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * POST   /api/contracts/upload          – upload file & start pipeline
 * GET    /api/contracts                 – paginated list
 * GET    /api/contracts/{id}/status     – polling endpoint
 * GET    /api/contracts/{id}/report     – full analysis report
 * DELETE /api/contracts/{id}            – delete contract + file
 * GET    /api/contracts/stats           – dashboard counts
 */
@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@Slf4j
public class ContractController {

    private final ContractService contractService;

    // ── Upload ────────────────────────────────────────────────

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<ContractUploadResponse>> upload(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        String clientIp = resolveClientIp(request);
        ContractUploadResponse response = contractService.upload(file, clientIp);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok(response, "File uploaded successfully. Analysis started."));
    }

    // ── List (paginated) ──────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ContractSummaryDto>>> list(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        if (size > 50) size = 50; // cap page size
        Page<ContractSummaryDto> contracts = contractService.listContracts(page, size);
        return ResponseEntity.ok(ApiResponse.ok(contracts));
    }

    // ── Status polling ────────────────────────────────────────

    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ContractStatusDto>> status(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(contractService.getStatus(id)));
    }

    // ── Full report ───────────────────────────────────────────

    @GetMapping("/{id}/report")
    public ResponseEntity<ApiResponse<AnalysisReportDto>> report(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(contractService.getReport(id)));
    }

    // ── Delete ────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        contractService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Contract deleted successfully."));
    }

    // ── Dashboard stats ───────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> stats() {
        return ResponseEntity.ok(ApiResponse.ok(contractService.getDashboardStats()));
    }

    // ── Private ───────────────────────────────────────────────

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
