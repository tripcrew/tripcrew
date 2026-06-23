package com.tripcrew.auth.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordVerificationRequest(
        @NotBlank @Size(max = 64)
        String currentPassword
) {
}
