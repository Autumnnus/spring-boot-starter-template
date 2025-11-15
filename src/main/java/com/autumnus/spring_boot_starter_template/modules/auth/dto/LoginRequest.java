package com.autumnus.spring_boot_starter_template.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "must be a valid email")
        @NotBlank(message = "email is required")
        @Schema(example = "admin@example.com")
        String email,

        @NotBlank(message = "password is required")
        @Schema(example = "Admin123!")
        String password,

        String deviceInfo,
        String ipAddress
) {
}
