package com.olie.api.inquiry;

import java.util.List;
import java.util.UUID;

/**
 * Tudo o que o disparador precisa para enviar a um contato, montado dentro da transação para que o envio
 * (lento, com pausas) aconteça fora dela.
 *
 * @param parts     mensagens na ordem de envio: o texto e depois uma por foto
 * @param sentParts quantas já foram entregues em tentativas anteriores (retoma daí)
 */
public record SendJob(
        UUID inquiryId,
        UUID recipientId,
        UUID userId,
        String phone,
        boolean simulated,
        List<Part> parts,
        int sentParts) {

    public record Part(String text, String photoPath, String photoContentType) {

        public boolean isPhoto() {
            return photoPath != null;
        }
    }
}
