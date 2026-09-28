package com.olie.api.dto.inquiry;

import java.util.List;
import java.util.UUID;

/** A sequência exata de mensagens que cada contato vai receber: o texto e, depois, uma mensagem por foto. */
public record InquiryPreviewResponse(String greeting, String question, List<Message> messages) {

    public enum MessageType {
        TEXT,
        PHOTO
    }

    public record Message(MessageType type, String text, UUID photoId) {
    }
}
