package com.olie.api.whatsapp;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import lombok.extern.slf4j.Slf4j;

/** Fala com o sidecar {@code whatsapp-gateway} (Node + Baileys) por HTTP. */
@Slf4j
public class GatewayWhatsAppProvider implements WhatsAppProvider {

    private record GatewaySession(String status, String qrCode, String phone, String reason) {
    }

    private record GatewayError(String code, String message) {
    }

    private final RestClient restClient;

    public GatewayWhatsAppProvider(WhatsAppProperties.Gateway gateway) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(gateway.timeout());
        requestFactory.setReadTimeout(gateway.timeout());

        this.restClient = RestClient.builder()
                .baseUrl(gateway.url())
                .defaultHeader("X-Gateway-Token", gateway.token())
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public WhatsAppConnection connection(UUID userId) {
        return fetchSession(() -> restClient.get().uri("/sessions/{id}", userId).retrieve().body(GatewaySession.class));
    }

    @Override
    public WhatsAppConnection connect(UUID userId) {
        return fetchSession(() -> restClient.post().uri("/sessions/{id}/connect", userId).retrieve()
                .body(GatewaySession.class));
    }

    @Override
    public WhatsAppConnection disconnect(UUID userId) {
        return fetchSession(() -> restClient.delete().uri("/sessions/{id}", userId).retrieve()
                .body(GatewaySession.class));
    }

    @Override
    public void sendText(UUID userId, String phone, String text) {
        send(userId, Map.of("to", phone, "text", text));
    }

    @Override
    public void sendImage(UUID userId, String phone, byte[] image, String contentType, String caption) {
        Map<String, Object> body = new HashMap<>();
        body.put("to", phone);
        body.put("image", Base64.getEncoder().encodeToString(image));
        body.put("mimetype", contentType);
        body.put("caption", caption);
        send(userId, body);
    }

    private void send(UUID userId, Map<String, ?> body) {
        try {
            restClient.post().uri("/sessions/{id}/messages", userId).body(body).retrieve().toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw translate(exception);
        } catch (ResourceAccessException exception) {
            throw new WhatsAppSendException(
                    WhatsAppSendException.Kind.TRANSIENT, "Gateway do WhatsApp indisponível", exception);
        }
    }

    private WhatsAppSendException translate(RestClientResponseException exception) {
        GatewayError error = exception.getResponseBodyAs(GatewayError.class);
        String code = error != null ? error.code() : null;
        String message = error != null && error.message() != null ? error.message() : exception.getMessage();
        HttpStatusCode status = exception.getStatusCode();

        if ("NOT_CONNECTED".equals(code)) {
            return new WhatsAppSendException(WhatsAppSendException.Kind.NOT_CONNECTED, "WhatsApp desconectado");
        }
        if ("NOT_ON_WHATSAPP".equals(code)) {
            return new WhatsAppSendException(WhatsAppSendException.Kind.PERMANENT, "Número sem conta no WhatsApp");
        }
        if (status.is4xxClientError()) {
            return new WhatsAppSendException(WhatsAppSendException.Kind.PERMANENT, message);
        }
        return new WhatsAppSendException(WhatsAppSendException.Kind.TRANSIENT, message);
    }

    private WhatsAppConnection fetchSession(Supplier<GatewaySession> call) {
        try {
            GatewaySession session = call.get();
            return new WhatsAppConnection(
                    WhatsAppConnectionStatus.valueOf(session.status()), session.qrCode(), session.phone(), session.reason());
        } catch (RestClientResponseException | ResourceAccessException exception) {
            log.warn("Falha ao consultar o gateway do WhatsApp: {}", exception.getMessage());
            return WhatsAppConnection.disconnected("GATEWAY_UNAVAILABLE");
        }
    }
}
