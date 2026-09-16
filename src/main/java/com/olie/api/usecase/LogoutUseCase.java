package com.olie.api.usecase;

import org.springframework.stereotype.Service;

import com.olie.api.service.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogoutUseCase {

    private final JwtService jwtService;

    public void execute(String token) {
        jwtService.revoke(token);
    }
}
