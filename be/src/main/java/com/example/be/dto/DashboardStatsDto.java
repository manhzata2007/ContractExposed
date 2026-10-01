package com.contractexposed.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Aggregate statistics for the dashboard overview.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardStatsDto {

    private long totalContracts;
    private long analyzedContracts;
    private long pendingContracts;
    private long failedContracts;
    private long criticalRiskContracts;
    private long highRiskContracts;
    private long mediumRiskContracts;
    private long lowRiskContracts;
}
