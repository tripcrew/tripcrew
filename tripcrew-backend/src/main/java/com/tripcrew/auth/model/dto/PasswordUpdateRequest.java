package com.tripcrew.auth.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordUpdateRequest(
        @NotBlank @Size(max = 64)
        String currentPassword,

        @NotBlank @Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
        String newPassword
) {
}
