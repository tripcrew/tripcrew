package com.tripcrew.auth.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameUpdateRequest(
        @NotBlank @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
        String nickname,

        // 소셜 계정은 비밀번호가 없으므로 선택값(LOCAL 계정만 서비스에서 검증).
        @Size(max = 64)
        String currentPassword
) {
}
