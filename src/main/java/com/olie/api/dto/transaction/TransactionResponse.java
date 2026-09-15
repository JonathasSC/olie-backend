package com.olie.api.dto.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.olie.api.entity.Transaction;
import com.olie.api.entity.TransactionType;

public record TransactionResponse(
        UUID id,
        BigDecimal value,
        String paymentMethod,
        TransactionType type,
        UUID categoryId,
        Instant createdAt) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getValue(),
                transaction.getPaymentMethod(),
                transaction.getType(),
                transaction.getCategory() != null ? transaction.getCategory().getId() : null,
                transaction.getCreatedAt());
    }
}
