package com.contractexposed.backend.dto;

import com.contractexposed.backend.entity.Contract;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Returned immediately after a file is uploaded.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractUploadResponse {

    private Long                     id;
    private String                   originalFileName;
    private String                   fileType;
    private Long                     fileSize;
    private Contract.ContractStatus  status;
    private LocalDateTime            createdAt;

    public static ContractUploadResponse from(Contract c) {
        return ContractUploadResponse.builder()
                .id(c.getId())
                .originalFileName(c.getOriginalFileName())
                .fileType(c.getFileType().name())
                .fileSize(c.getFileSize())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
