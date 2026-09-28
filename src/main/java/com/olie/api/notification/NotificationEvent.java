package com.olie.api.notification;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.olie.api.entity.NotificationType;
import com.olie.api.entity.PlannedItem;
import com.olie.api.entity.User;
import com.olie.api.entity.WearItem;

public record NotificationEvent(
        UUID id,
        UUID userId,
        String userEmail,
        NotificationType type,
        String message,
        UUID plannedItemId,
        UUID wearItemId,
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

    public static NotificationEvent wearItemReplacementApproaching(WearItem wearItem, LocalDate replacementDate) {
        String message = "O item \"%s\" está chegando ao fim da vida útil — troca estimada para %s."
                .formatted(wearItem.getName(), replacementDate);
        return new NotificationEvent(
                UUID.randomUUID(),
                wearItem.getUser().getId(),
                wearItem.getUser().getEmail(),
                NotificationType.WEAR_ITEM_REPLACEMENT_APPROACHING,
                message,
                null,
                wearItem.getId(),
                Instant.now());
    }

    public static NotificationEvent whatsAppDisconnected(User user) {
        String message = "Sua sessão do WhatsApp foi desconectada pelo celular. Conecte novamente para voltar a enviar consultas.";
        return ofUser(user, NotificationType.WHATSAPP_DISCONNECTED, message);
    }

    public static NotificationEvent inquiryFinished(User user, long sent, long failed) {
        String message = failed == 0
                ? "Consulta concluída: %d contato(s) receberam a mensagem.".formatted(sent)
                : "Consulta concluída: %d enviada(s) e %d com falha.".formatted(sent, failed);
        return ofUser(user, NotificationType.INQUIRY_FINISHED, message);
    }

    private static NotificationEvent ofUser(User user, NotificationType type, String message) {
        return new NotificationEvent(
                UUID.randomUUID(), user.getId(), user.getEmail(), type, message, null, null, Instant.now());
    }

    private static NotificationEvent of(PlannedItem plannedItem, NotificationType type, String message) {
        return new NotificationEvent(
                UUID.randomUUID(),
                plannedItem.getUser().getId(),
                plannedItem.getUser().getEmail(),
                type,
                message,
                plannedItem.getId(),
                null,
                Instant.now());
    }
}
