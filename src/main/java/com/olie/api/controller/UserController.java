package com.olie.api.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olie.api.dto.UserResponse;
import com.olie.api.entity.User;
import com.olie.api.usecase.GetCurrentUserUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiV1.PREFIX + "/me")
@RequiredArgsConstructor
public class UserController {

    private final GetCurrentUserUseCase getCurrentUserUseCase;

    @GetMapping
    public UserResponse me(@AuthenticationPrincipal User user) {
        return getCurrentUserUseCase.execute(user);
    }
}
