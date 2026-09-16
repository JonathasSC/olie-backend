package com.olie.api.dto.savingsgoal;

import java.math.BigDecimal;
import java.util.UUID;

import com.olie.api.entity.SavingsGoal;
import com.olie.api.entity.SavingsGoalPeriod;

public record SavingsGoalResponse(
        UUID id, String name, BigDecimal amount, SavingsGoalPeriod period, UUID plannedItemId) {

    public static SavingsGoalResponse from(SavingsGoal savingsGoal) {
        return new SavingsGoalResponse(
                savingsGoal.getId(),
                savingsGoal.getName(),
                savingsGoal.getAmount(),
                savingsGoal.getPeriod(),
                savingsGoal.getPlannedItem() != null ? savingsGoal.getPlannedItem().getId() : null);
    }
}
