package com.olie.api.inquiry;

import java.util.List;

/**
 * Mensagem pronta para um contato: o texto principal e, na mesma ordem dos itens, a legenda que acompanha a
 * foto de cada um (usada só para itens com foto).
 */
public record ComposedMessage(String greeting, String question, String text, List<String> itemCaptions) {
}
