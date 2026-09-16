package com.olie.api.notification;

import java.time.Instant;
import java.util.UUID;

import com.olie.api.entity.NotificationType;
import com.olie.api.entity.PlannedItem;

public record NotificationEvent(
        UUID id,
        UUID userId,
        String userEmail,
        NotificationType type,
        String message,
        UUID plannedItemId,
        Instant createdAt) {

    public static NotificationEvent purchaseDateApproaching(PlannedItem plannedItem) {
        String message = "A data estimada da compra \"%s\" está se aproximando (%s)."
                .formatted(plannedItem.getName(), plannedItem.getEstimatedDate());
        return of(plannedItem, NotificationType.PURCHASE_DATE_APPROACHING, message);
    }

    public static NotificationEvent sufficientBalance(PlannedItem plannedItem) {
        String message = "Você já tem saldo suficiente na categoria \"%s\" para realizar a compra \"%s\"."
                .formatted(plannedItem.getCategory().getName(), plannedItem.getName());
        return of(plannedItem, NotificationType.SUFFICIENT_BALANCE, message);
    }

    private static NotificationEvent of(PlannedItem plannedItem, NotificationType type, String message) {
        return new NotificationEvent(
                UUID.randomUUID(),
                plannedItem.getUser().getId(),
                plannedItem.getUser().getEmail(),
                type,
                message,
                plannedItem.getId(),
                Instant.now());
    }
}
