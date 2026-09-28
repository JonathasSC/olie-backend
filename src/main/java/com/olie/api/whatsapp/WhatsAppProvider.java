package com.olie.api.whatsapp;

import java.util.UUID;

/**
 * Fronteira com o WhatsApp: o restante do sistema só conhece esta interface, o que permite trocar o
 * provedor (WhatsApp Web via gateway, Cloud API oficial, simulado) sem mexer nas funcionalidades.
 * Cada usuário do Olie tem a própria sessão, identificada pelo seu id.
 */
public interface WhatsAppProvider {

    WhatsAppConnection connection(UUID userId);

    /** Inicia a conexão; com sessão salva conecta direto, senão passa a exibir QR code. */
    WhatsAppConnection connect(UUID userId);

    /** Encerra a sessão e apaga as credenciais salvas. */
    WhatsAppConnection disconnect(UUID userId);

    void sendText(UUID userId, String phone, String text);

    void sendImage(UUID userId, String phone, byte[] image, String contentType, String caption);
}
