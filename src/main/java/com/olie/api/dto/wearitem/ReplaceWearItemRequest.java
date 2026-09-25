package com.olie.api.dto.wearitem;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

public record ReplaceWearItemRequest(
        @NotNull @PastOrPresent LocalDate purchaseDate,
        @PastOrPresent LocalDate installationDate,
        LocalDate removalDate,
        @Positive BigDecimal purchaseValue,
        String paymentMethod) {
}
