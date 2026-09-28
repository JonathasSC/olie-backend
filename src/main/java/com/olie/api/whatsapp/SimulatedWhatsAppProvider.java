package com.olie.api.whatsapp;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.extern.slf4j.Slf4j;

/**
 * Provedor que não envia nada: sempre "conectado" e apenas registra as mensagens. Usado pelo modo de
 * simulação das consultas, em testes e quando {@code whatsapp.provider=simulated}.
 */
@Slf4j
public class SimulatedWhatsAppProvider implements WhatsAppProvider {

    public record SentMessage(UUID userId, String phone, String text, String caption, boolean image) {
    }

    private final List<SentMessage> sentMessages = new CopyOnWriteArrayList<>();

    @Override
    public WhatsAppConnection connection(UUID userId) {
        return new WhatsAppConnection(WhatsAppConnectionStatus.CONNECTED, null, "+5500000000000", null);
    }

    @Override
    public WhatsAppConnection connect(UUID userId) {
        return connection(userId);
    }

    @Override
    public WhatsAppConnection disconnect(UUID userId) {
        return connection(userId);
    }

    @Override
    public void sendText(UUID userId, String phone, String text) {
        log.debug("[simulado] texto para {}: {}", phone, text);
        sentMessages.add(new SentMessage(userId, phone, text, null, false));
    }

    @Override
    public void sendImage(UUID userId, String phone, byte[] image, String contentType, String caption) {
        log.debug("[simulado] foto para {} ({} bytes): {}", phone, image.length, caption);
        sentMessages.add(new SentMessage(userId, phone, null, caption, true));
    }

    public List<SentMessage> getSentMessages() {
        return List.copyOf(sentMessages);
    }
}
