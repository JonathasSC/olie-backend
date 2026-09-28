package com.olie.api.inquiry;

import java.nio.file.Path;
import java.time.Duration;
import java.time.ZoneId;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

/**
 * Limites e intervalos das consultas via WhatsApp. Os padrões são conservadores de propósito: volume e ritmo
 * de envio são o que mais pesa para o WhatsApp bloquear um número.
 */
@ConfigurationProperties(prefix = "inquiries")
public record InquiryProperties(
        ZoneId timezone,
        String messageTemplate,
        int maxItems,
        int maxRecipients,
        Duration contactIntervalMin,
        Duration contactIntervalMax,
        Duration messageInterval,
        int maxAttempts,
        Duration retryDelay,
        Duration duplicateWindow,
        Duration simulationInterval,
        Photos photos) {

    public static final String GREETING = "{saudacao}";
    public static final String QUESTION = "{pergunta}";
    public static final String ITEMS = "{itens}";

    public record Photos(
            Path storageDir,
            DataSize maxUploadSize,
            DataSize maxSize,
            int maxDimension,
            Duration orphanRetention) {
    }

    public InquiryProperties {
        for (String variable : new String[] {GREETING, QUESTION, ITEMS}) {
            if (messageTemplate == null || !messageTemplate.contains(variable)) {
                throw new IllegalArgumentException(
                        "inquiries.message-template precisa conter a variável " + variable);
            }
        }
        if (contactIntervalMin.compareTo(contactIntervalMax) > 0) {
            throw new IllegalArgumentException(
                    "inquiries.contact-interval-min não pode ser maior que inquiries.contact-interval-max");
        }
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("inquiries.max-attempts deve ser pelo menos 1");
        }
    }
}
