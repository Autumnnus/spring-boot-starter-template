package com.autumnus.spring_boot_starter_template.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "must be a valid email")
        @NotBlank(message = "email is required")
        String email,

        @NotBlank(message = "password is required")
        String password,

        String deviceInfo,
        String ipAddress
) {
}
