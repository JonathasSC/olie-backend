package com.olie.api.usecase.contact;

import java.util.Optional;
import java.util.regex.Pattern;

public final class PhoneNumbers {

    private static final Pattern E164 = Pattern.compile("^\\+[1-9]\\d{7,14}$");
    private static final Pattern FORMATTING = Pattern.compile("[\\s().-]");

    private PhoneNumbers() {
    }

    /**
     * Aceita o número com a formatação usual ("+55 (11) 99999-9999") e devolve em E.164
     * ("+5511999999999"). O código do país é obrigatório: sem o "+" o número é ambíguo.
     */
    public static Optional<String> normalize(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String phone = FORMATTING.matcher(raw.trim()).replaceAll("");
        return E164.matcher(phone).matches() ? Optional.of(phone) : Optional.empty();
    }
}
