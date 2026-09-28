package com.olie.api.inquiry;

import java.time.Clock;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

@Component
public class InquiryMessageComposer {

    private static final LocalTime MORNING_START = LocalTime.of(5, 0);
    private static final LocalTime AFTERNOON_START = LocalTime.of(12, 0);
    private static final LocalTime EVENING_START = LocalTime.of(18, 0);

    private final InquiryProperties properties;
    private final Clock clock;

    public InquiryMessageComposer(InquiryProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    /** Compõe a mensagem com a saudação do horário atual no fuso configurado. */
    public ComposedMessage compose(List<String> itemNames) {
        return compose(itemNames, ZonedDateTime.now(clock.withZone(properties.timezone())).toLocalTime());
    }

    public ComposedMessage compose(List<String> itemNames, LocalTime localTime) {
        String greeting = greeting(localTime);
        String question = itemNames.size() == 1 ? "Tem esse item?" : "Tem esses itens?";
        List<String> numbered = IntStream.range(0, itemNames.size())
                .mapToObj(index -> "%d. %s".formatted(index + 1, itemNames.get(index).trim()))
                .toList();

        String text = properties.messageTemplate()
                .replace(InquiryProperties.GREETING, greeting)
                .replace(InquiryProperties.QUESTION, question)
                .replace(InquiryProperties.ITEMS, numbered.stream().collect(Collectors.joining("\n")));

        return new ComposedMessage(greeting, question, text, numbered);
    }

    static String greeting(LocalTime time) {
        if (!time.isBefore(MORNING_START) && time.isBefore(AFTERNOON_START)) {
            return "Bom dia";
        }
        if (!time.isBefore(AFTERNOON_START) && time.isBefore(EVENING_START)) {
            return "Boa tarde";
        }
        return "Boa noite";
    }
}
