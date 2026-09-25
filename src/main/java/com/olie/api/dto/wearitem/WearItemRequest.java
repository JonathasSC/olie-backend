package com.olie.api.dto.wearitem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.olie.api.entity.WearItemLifespanUnit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

public record WearItemRequest(
        @NotBlank String name,
        UUID categoryId,
        @NotNull @Positive Integer expectedLifespan,
        @NotNull WearItemLifespanUnit expectedLifespanUnit,
        @NotNull @PastOrPresent LocalDate purchaseDate,
        @PastOrPresent LocalDate installationDate,
        @Positive BigDecimal purchaseValue) {
}
