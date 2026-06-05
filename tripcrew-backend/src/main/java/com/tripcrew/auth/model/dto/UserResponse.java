package com.tripcrew.auth.model.dto;

import com.tripcrew.user.model.dto.User;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        String role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name()
        );
    }
}
