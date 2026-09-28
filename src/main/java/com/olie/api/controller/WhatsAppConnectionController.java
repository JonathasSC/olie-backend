package com.olie.api.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olie.api.entity.User;
import com.olie.api.whatsapp.WhatsAppConnection;
import com.olie.api.whatsapp.WhatsAppProvider;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiV1.PREFIX + "/whatsapp/connection")
@RequiredArgsConstructor
public class WhatsAppConnectionController {

    private final WhatsAppProvider whatsAppProvider;

    @GetMapping
    public WhatsAppConnection get(@AuthenticationPrincipal User user) {
        return whatsAppProvider.connection(user.getId());
    }

    @PostMapping
    public WhatsAppConnection connect(@AuthenticationPrincipal User user) {
        return whatsAppProvider.connect(user.getId());
    }

    @DeleteMapping
    public WhatsAppConnection disconnect(@AuthenticationPrincipal User user) {
        return whatsAppProvider.disconnect(user.getId());
    }
}
