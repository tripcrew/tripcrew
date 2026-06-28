package com.tripcrew.auth.model.dto;

import jakarta.validation.constraints.NotBlank;

/** 소셜 로그인 일회용 코드 → JWT 교환 요청. */
public record OAuthExchangeRequest(
        @NotBlank
        String code
) {
}
