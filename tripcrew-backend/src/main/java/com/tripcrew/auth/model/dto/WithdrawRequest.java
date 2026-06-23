package com.tripcrew.auth.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WithdrawRequest(
        @NotBlank @Size(max = 64)
        String currentPassword
) {
}
