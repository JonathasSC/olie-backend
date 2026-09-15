package com.olie.api.dto;

import java.util.UUID;

import com.olie.api.entity.Role;

public record UserResponse(UUID id, String name, String email, Role role) {
}
