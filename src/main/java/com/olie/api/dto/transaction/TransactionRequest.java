package com.olie.api.dto.transaction;

import java.math.BigDecimal;
import java.util.UUID;

import com.olie.api.entity.TransactionType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionRequest(
        @NotNull @Positive BigDecimal value,
        @NotBlank String paymentMethod,
        @NotNull TransactionType type,
        UUID categoryId) {
}
