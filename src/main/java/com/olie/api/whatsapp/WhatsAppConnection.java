package com.olie.api.whatsapp;

/**
 * Estado da sessão de WhatsApp de um usuário.
 *
 * @param qrCode imagem PNG em data URL, presente só em {@code WAITING_QR}
 * @param phone  número conectado (E.164), presente só em {@code CONNECTED}
 * @param reason motivo da última desconexão: {@code LOGGED_OUT}, {@code QR_TIMEOUT} ou {@code CONNECTION_LOST}
 */
public record WhatsAppConnection(WhatsAppConnectionStatus status, String qrCode, String phone, String reason) {

    public static WhatsAppConnection disconnected(String reason) {
        return new WhatsAppConnection(WhatsAppConnectionStatus.DISCONNECTED, null, null, reason);
    }

    public boolean isConnected() {
        return status == WhatsAppConnectionStatus.CONNECTED;
    }
}
