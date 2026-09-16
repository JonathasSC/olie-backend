package com.olie.api.dto.planneditem;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CompletePlannedItemRequest(@NotBlank String paymentMethod, @Positive BigDecimal value) {
}
