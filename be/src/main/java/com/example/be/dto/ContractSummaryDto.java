package com.contractexposed.backend.dto;

import com.contractexposed.backend.entity.AnalysisReport;
import com.contractexposed.backend.entity.Contract;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Lightweight contract row for the list/history page.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractSummaryDto {

    private Long                     id;
    private String                   originalFileName;
    private String                   fileType;
    private Long                     fileSize;
    private Contract.ContractStatus  status;
    private Integer                  overallRiskScore;
    private AnalysisReport.RiskLevel riskLevel;
    private LocalDateTime            createdAt;
    private LocalDateTime            updatedAt;

    public static ContractSummaryDto from(Contract c) {
        ContractSummaryDtoBuilder b = ContractSummaryDto.builder()
                .id(c.getId())
                .originalFileName(c.getOriginalFileName())
                .fileType(c.getFileType().name())
                .fileSize(c.getFileSize())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt());

        if (c.getAnalysisReport() != null) {
            b.overallRiskScore(c.getAnalysisReport().getOverallRiskScore());
            b.riskLevel(c.getAnalysisReport().getRiskLevel());
        }
        return b.build();
    }
}
