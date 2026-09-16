package com.olie.api.dto.savingsgoal;

import java.math.BigDecimal;
import java.util.UUID;

import com.olie.api.entity.SavingsGoalPeriod;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SavingsGoalRequest(
        @NotBlank String name,
        @NotNull @Positive BigDecimal amount,
        @NotNull SavingsGoalPeriod period,
        UUID plannedItemId) {
}
