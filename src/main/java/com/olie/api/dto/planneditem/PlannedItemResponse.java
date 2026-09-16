package com.olie.api.dto.planneditem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.olie.api.entity.PlannedItem;
import com.olie.api.entity.PlannedItemPriority;

public record PlannedItemResponse(
        UUID id,
        String name,
        PlannedItemPriority priority,
        BigDecimal estimatedValue,
        LocalDate estimatedDate,
        UUID categoryId) {

    public static PlannedItemResponse from(PlannedItem plannedItem) {
        return new PlannedItemResponse(
                plannedItem.getId(),
                plannedItem.getName(),
                plannedItem.getPriority(),
                plannedItem.getEstimatedValue(),
                plannedItem.getEstimatedDate(),
                plannedItem.getCategory() != null ? plannedItem.getCategory().getId() : null);
    }
}
