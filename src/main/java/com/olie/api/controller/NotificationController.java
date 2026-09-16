package com.olie.api.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olie.api.dto.notification.NotificationResponse;
import com.olie.api.entity.User;
import com.olie.api.usecase.notification.ListNotificationsUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiV1.PREFIX + "/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final ListNotificationsUseCase listNotificationsUseCase;

    @GetMapping
    public List<NotificationResponse> list(@AuthenticationPrincipal User user) {
        return listNotificationsUseCase.execute(user);
    }
}
