package com.contractexposed.backend.controller;

import com.contractexposed.backend.dto.ApiResponse;
import com.contractexposed.backend.dto.ChecklistUpdateRequest;
import com.contractexposed.backend.dto.NegotiationChecklistItemDto;
import com.contractexposed.backend.service.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PATCH /api/checklist/{itemId} – toggle checklist item completion
 */
@RestController
@RequestMapping("/api/checklist")
@RequiredArgsConstructor
public class ChecklistController {

    private final ContractService contractService;

    @PatchMapping("/{itemId}")
    public ResponseEntity<ApiResponse<NegotiationChecklistItemDto>> toggle(
            @PathVariable Long itemId,
            @Valid @RequestBody ChecklistUpdateRequest request) {

        NegotiationChecklistItemDto updated =
                contractService.toggleChecklistItem(itemId, request.getIsCompleted());
        return ResponseEntity.ok(ApiResponse.ok(updated, "Checklist item updated."));
    }
}
