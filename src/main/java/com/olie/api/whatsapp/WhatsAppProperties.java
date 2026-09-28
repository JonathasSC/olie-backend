package com.olie.api.whatsapp;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "whatsapp")
public record WhatsAppProperties(String provider, Gateway gateway) {

    public record Gateway(String url, String token, Duration timeout) {
    }
}
