package com.contractexposed.backend.dto;

import com.contractexposed.backend.entity.NegotiationChecklistItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * One action item in the negotiation checklist.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NegotiationChecklistItemDto {

    private Long                           id;
    private String                         itemText;
    private NegotiationChecklistItem.Priority priority;
    private String                         category;
    private Boolean                        isCompleted;
    private Integer                        sortOrder;

    public static NegotiationChecklistItemDto from(NegotiationChecklistItem item) {
        return NegotiationChecklistItemDto.builder()
                .id(item.getId())
                .itemText(item.getItemText())
                .priority(item.getPriority())
                .category(item.getCategory())
                .isCompleted(item.getIsCompleted())
                .sortOrder(item.getSortOrder())
                .build();
    }
}
