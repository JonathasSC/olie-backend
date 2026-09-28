package com.olie.api.inquiry;

import java.nio.file.Path;
import java.time.Duration;
import java.time.ZoneId;

import org.springframework.util.unit.DataSize;

final class InquiryTestProperties {

    private InquiryTestProperties() {
    }

    static InquiryProperties create(Path storageDir) {
        return new InquiryProperties(
                ZoneId.of("America/Sao_Paulo"),
                "{saudacao}! {pergunta}\n\n{itens}",
                10,
                30,
                Duration.ofSeconds(20),
                Duration.ofSeconds(60),
                Duration.ZERO,
                3,
                Duration.ZERO,
                Duration.ofDays(7),
                Duration.ofSeconds(1),
                new InquiryProperties.Photos(storageDir, DataSize.ofMegabytes(10), DataSize.ofKilobytes(200), 800,
                        Duration.ofDays(1)));
    }
}
