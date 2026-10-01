package com.contractexposed.backend.dto;

import com.contractexposed.backend.entity.RiskFinding;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Represents a single risky clause in the analysis report.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiskFindingDto {

    private Long                  id;
    private String                clauseTitle;
    private String                originalText;
    private RiskFinding.RiskType  riskType;
    private RiskFinding.Severity  severity;
    private Integer               severityScore;
    private String                explanation;
    private String                recommendation;
    private Integer               pageNumber;
    private String                highlightColor;
    private Integer               sortOrder;

    public static RiskFindingDto from(RiskFinding f) {
        return RiskFindingDto.builder()
                .id(f.getId())
                .clauseTitle(f.getClauseTitle())
                .originalText(f.getOriginalText())
                .riskType(f.getRiskType())
                .severity(f.getSeverity())
                .severityScore(f.getSeverityScore())
                .explanation(f.getExplanation())
                .recommendation(f.getRecommendation())
                .pageNumber(f.getPageNumber())
                .highlightColor(f.getSeverity() != null
                        ? f.getSeverity().getColor()
                        : f.getHighlightColor())
                .sortOrder(f.getSortOrder())
                .build();
    }
}
