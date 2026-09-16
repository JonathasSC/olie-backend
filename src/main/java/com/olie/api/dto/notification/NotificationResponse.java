package com.olie.api.dto.notification;

import java.time.Instant;
import java.util.UUID;

import com.olie.api.entity.Notification;
import com.olie.api.entity.NotificationType;

public record NotificationResponse(
        UUID id, NotificationType type, String message, UUID plannedItemId, Instant sentAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getPlannedItem() != null ? notification.getPlannedItem().getId() : null,
                notification.getSentAt());
    }
}
