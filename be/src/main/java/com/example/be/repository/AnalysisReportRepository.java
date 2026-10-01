package com.contractexposed.backend.repository;

import com.contractexposed.backend.entity.AnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, Long> {

    Optional<AnalysisReport> findByContractId(Long contractId);

    // ── Step 1: fetch report + findings ──────────────────────
    @Query("""
        SELECT r FROM AnalysisReport r
        LEFT JOIN FETCH r.findings
        WHERE r.contract.id = :contractId
        """)
    Optional<AnalysisReport> findByContractIdWithFindings(@Param("contractId") Long contractId);

    // ── Step 2: fetch report + checklistItems ─────────────────
    @Query("""
        SELECT r FROM AnalysisReport r
        LEFT JOIN FETCH r.checklistItems
        WHERE r.contract.id = :contractId
        """)
    Optional<AnalysisReport> findByContractIdWithChecklist(@Param("contractId") Long contractId);

    boolean existsByContractId(Long contractId);
}
