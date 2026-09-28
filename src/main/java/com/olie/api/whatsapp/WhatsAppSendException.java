package com.olie.api.whatsapp;

import lombok.Getter;

@Getter
public class WhatsAppSendException extends RuntimeException {

    public enum Kind {
        /** Sessão caiu: o envio deve ser pausado até o usuário reconectar. */
        NOT_CONNECTED,
        /** Falha passageira (rede, instabilidade do WhatsApp): vale tentar de novo. */
        TRANSIENT,
        /** Falha definitiva para este contato (ex.: número sem WhatsApp): não adianta repetir. */
        PERMANENT
    }

    private final Kind kind;

    public WhatsAppSendException(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    public WhatsAppSendException(Kind kind, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
    }
}
