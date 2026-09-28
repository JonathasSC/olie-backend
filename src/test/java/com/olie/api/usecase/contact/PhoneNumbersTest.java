package com.olie.api.usecase.contact;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PhoneNumbersTest {

    @ParameterizedTest
    @CsvSource({
            "+5511999999999, +5511999999999",
            "'+55 (11) 99999-9999', +5511999999999",
            "' +55.11.3333.4444 ', +551133334444",
            "+14155552671, +14155552671"
    })
    void normalizesToE164(String raw, String expected) {
        assertThat(PhoneNumbers.normalize(raw)).contains(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"11999999999", "+0511999999999", "+55 11 999", "+55119999999999999", "+55abc99999999"})
    void rejectsInvalidNumbers(String raw) {
        assertThat(PhoneNumbers.normalize(raw)).isEmpty();
    }
}
