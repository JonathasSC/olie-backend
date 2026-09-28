package com.olie.api.inquiry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InquiryMessageComposerTest {

    private final InquiryProperties properties = InquiryTestProperties.create(Path.of("unused"));
    private final InquiryMessageComposer composer = new InquiryMessageComposer(properties, Clock.systemUTC());

    @ParameterizedTest
    @CsvSource({
            "05:00, Bom dia",
            "11:59, Bom dia",
            "12:00, Boa tarde",
            "17:59, Boa tarde",
            "18:00, Boa noite",
            "23:59, Boa noite",
            "00:00, Boa noite",
            "04:59, Boa noite"
    })
    void greetingFollowsLocalTimeBoundaries(LocalTime time, String expected) {
        assertThat(InquiryMessageComposer.greeting(time)).isEqualTo(expected);
    }

    @Test
    void singleItemUsesSingularQuestion() {
        ComposedMessage message = composer.compose(List.of("Dipirona 500mg"), LocalTime.of(9, 0));

        assertThat(message.text()).isEqualTo("Bom dia! Tem esse item?\n\n1. Dipirona 500mg");
        assertThat(message.itemCaptions()).containsExactly("1. Dipirona 500mg");
    }

    @Test
    void multipleItemsUsePluralAndAreNumbered() {
        ComposedMessage message = composer.compose(List.of(" Parafuso 8mm ", "Bucha 8"), LocalTime.of(19, 30));

        assertThat(message.text()).isEqualTo("Boa noite! Tem esses itens?\n\n1. Parafuso 8mm\n2. Bucha 8");
        assertThat(message.itemCaptions()).containsExactly("1. Parafuso 8mm", "2. Bucha 8");
    }

    @Test
    void usesConfiguredTimezone() {
        // 15:00 UTC = 12:00 em São Paulo
        Clock clock = Clock.fixed(Instant.parse("2026-09-28T15:00:00Z"), ZoneOffset.UTC);
        ComposedMessage message = new InquiryMessageComposer(properties, clock).compose(List.of("Item"));

        assertThat(message.greeting()).isEqualTo("Boa tarde");
    }

    @Test
    void rejectsTemplateWithoutRequiredVariables() {
        assertThatThrownBy(() -> new InquiryProperties(properties.timezone(), "{saudacao}! {itens}",
                10, 30, properties.contactIntervalMin(), properties.contactIntervalMax(),
                properties.messageInterval(), 3, properties.retryDelay(), properties.duplicateWindow(),
                properties.simulationInterval(), properties.photos()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("{pergunta}");
    }
}
