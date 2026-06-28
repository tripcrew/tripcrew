package com.tripcrew.auth.model.dto;

import jakarta.validation.constraints.Size;

public record WithdrawRequest(
        // 소셜 계정은 비밀번호가 없으므로 선택값(LOCAL 계정만 서비스에서 검증).
        @Size(max = 64)
        String currentPassword
) {
}
