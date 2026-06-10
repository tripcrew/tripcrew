package com.tripcrew.auth.model.dto;

public record TokenResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        UserResponse user
) {
    public static TokenResponse of(String accessToken, String refreshToken, UserResponse user) {
        return new TokenResponse("Bearer", accessToken, refreshToken, user);
    }
}
