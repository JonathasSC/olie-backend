package com.olie.api.dto.account;

public record AuthResponse(String token, String tokenType) {

    public static AuthResponse bearer(String token) {
        return new AuthResponse(token, "Bearer");
    }
}
