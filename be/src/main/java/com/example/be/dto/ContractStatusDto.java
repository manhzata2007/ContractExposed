package com.contractexposed.backend.dto;

import com.contractexposed.backend.entity.Contract;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Lightweight polling response — frontend polls this while analyzing.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractStatusDto {

    private Long                    id;
    private Contract.ContractStatus status;
    private String                  errorMessage;
    private boolean                 reportReady;

    public static ContractStatusDto from(Contract c) {
        boolean ready = c.getStatus() == Contract.ContractStatus.ANALYZED
                     && c.getAnalysisReport() != null;
        return ContractStatusDto.builder()
                .id(c.getId())
                .status(c.getStatus())
                .errorMessage(c.getErrorMessage())
                .reportReady(ready)
                .build();
    }
}
