package com.contractexposed.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body for toggling a checklist item's completion status.
 */
@Getter
@Setter
public class ChecklistUpdateRequest {

    @NotNull(message = "isCompleted is required")
    private Boolean isCompleted;
}
