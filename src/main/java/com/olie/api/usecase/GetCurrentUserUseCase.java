package com.olie.api.usecase;

import org.springframework.stereotype.Service;

import com.olie.api.dto.UserResponse;
import com.olie.api.entity.User;

@Service
public class GetCurrentUserUseCase {

    public UserResponse execute(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
