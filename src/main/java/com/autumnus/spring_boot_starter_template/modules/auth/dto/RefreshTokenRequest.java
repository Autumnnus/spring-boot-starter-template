package com.autumnus.spring_boot_starter_template.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "refreshToken is required")
        String refreshToken
) {
}
