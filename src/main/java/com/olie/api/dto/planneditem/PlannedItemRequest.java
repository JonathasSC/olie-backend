package com.olie.api.dto.planneditem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.olie.api.entity.PlannedItemPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PlannedItemRequest(
        @NotBlank String name,
        @NotNull PlannedItemPriority priority,
        @NotNull @Positive BigDecimal estimatedValue,
        @NotNull LocalDate estimatedDate,
        UUID categoryId) {
}
