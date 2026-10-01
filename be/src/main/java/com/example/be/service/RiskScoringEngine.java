package com.contractexposed.backend.service;

import com.contractexposed.backend.entity.AnalysisReport;
import com.contractexposed.backend.entity.RiskFinding;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Calculates the overall risk score (0-100) for a report
 * based on its individual findings and their severities.
 */
@Component
public class RiskScoringEngine {

    // Weights per severity level (sum of all findings, capped at 100)
    private static final int WEIGHT_CRITICAL = 30;
    private static final int WEIGHT_HIGH     = 15;
    private static final int WEIGHT_MEDIUM   =  8;
    private static final int WEIGHT_LOW      =  3;

    /**
     * Computes score and sets riskLevel on the report.
     */
    public void score(AnalysisReport report) {
        List<RiskFinding> findings = report.getFindings();
        if (findings == null || findings.isEmpty()) {
            report.setOverallRiskScore(0);
            report.setRiskLevel(AnalysisReport.RiskLevel.UNKNOWN);
            return;
        }

        int rawScore = findings.stream().mapToInt(f -> switch (f.getSeverity()) {
            case CRITICAL -> WEIGHT_CRITICAL;
            case HIGH     -> WEIGHT_HIGH;
            case MEDIUM   -> WEIGHT_MEDIUM;
            case LOW      -> WEIGHT_LOW;
        }).sum();

        // Cap at 100; also apply a floor boost if there's at least 1 critical finding
        int score = Math.min(rawScore, 100);
        long criticalCount = findings.stream()
                .filter(f -> f.getSeverity() == RiskFinding.Severity.CRITICAL)
                .count();
        if (criticalCount > 0) {
            score = Math.max(score, 80); // critical finding guarantees HIGH risk floor
        }

        report.setOverallRiskScore(score);
        report.setRiskLevel(AnalysisReport.RiskLevel.fromScore(score));

        // Set per-finding severity scores if AI didn't provide them
        int[] order = {0};
        findings.forEach(f -> {
            if (f.getSeverityScore() == 0) {
                f.setSeverityScore(f.getSeverity().getDefaultScore());
            }
            if (f.getHighlightColor() == null || f.getHighlightColor().isBlank()) {
                f.setHighlightColor(f.getSeverity().getColor());
            }
            f.setSortOrder(order[0]++);
        });
    }
}
