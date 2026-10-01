package com.contractexposed.backend.repository;

import com.contractexposed.backend.entity.RiskFinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskFindingRepository extends JpaRepository<RiskFinding, Long> {

    List<RiskFinding> findByReportIdOrderBySortOrderAscSeverityScoreDesc(Long reportId);

    List<RiskFinding> findByReportIdAndSeverity(Long reportId, RiskFinding.Severity severity);

    long countByReportIdAndSeverity(Long reportId, RiskFinding.Severity severity);
}
