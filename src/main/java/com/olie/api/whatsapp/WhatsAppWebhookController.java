package com.olie.api.whatsapp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.olie.api.notification.NotificationEvent;
import com.olie.api.notification.NotificationEventProducer;
import com.olie.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Recebe do gateway as mudanças de estado da sessão (QR novo, conectou, caiu) e repassa ao usuário via
 * WebSocket. Fica fora do /api/v1 e sem JWT: a autenticação é o segredo compartilhado com o gateway.
 */
@RestController
@RequiredArgsConstructor
public class WhatsAppWebhookController {

    public static final String PATH = "/internal/whatsapp/events";
    public static final String USER_DESTINATION = "/queue/whatsapp";

    record GatewayEvent(String event, UUID sessionId, String status, String qrCode, String phone, String reason) {
    }

    private final WhatsAppProperties properties;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationEventProducer notificationEventProducer;

    @PostMapping(PATH)
    public ResponseEntity<Void> receive(
            @RequestHeader(name = "X-Gateway-Token", required = false) String token, @RequestBody GatewayEvent event) {
        if (!validToken(token)) {
            return ResponseEntity.status(401).build();
        }
        if (!"connection".equals(event.event()) || event.sessionId() == null) {
            return ResponseEntity.accepted().build();
        }

        userRepository.findById(event.sessionId()).ifPresent(user -> {
            WhatsAppConnection connection = new WhatsAppConnection(
                    WhatsAppConnectionStatus.valueOf(event.status()), event.qrCode(), event.phone(), event.reason());
            messagingTemplate.convertAndSendToUser(user.getEmail(), USER_DESTINATION, connection);

            // queda definitiva (aparelho desconectado pelo celular) exige ação do usuário: vira notificação
            if ("LOGGED_OUT".equals(event.reason())) {
                notificationEventProducer.publish(NotificationEvent.whatsAppDisconnected(user));
            }
        });

        return ResponseEntity.noContent().build();
    }

    private boolean validToken(String token) {
        return token != null && MessageDigest.isEqual(
                token.getBytes(StandardCharsets.UTF_8),
                properties.gateway().token().getBytes(StandardCharsets.UTF_8));
    }
}
