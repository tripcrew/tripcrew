package com.tripcrew.auth.model.dto;

import jakarta.validation.constraints.NotBlank;

public record ReissueRequest(

        @NotBlank
        String refreshToken
) {
}
