package com.olie.api.whatsapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WhatsAppConfig {

    @Bean
    public WhatsAppProvider whatsAppProvider(WhatsAppProperties properties) {
        if ("simulated".equalsIgnoreCase(properties.provider())) {
            return new SimulatedWhatsAppProvider();
        }
        return new GatewayWhatsAppProvider(properties.gateway());
    }
}
