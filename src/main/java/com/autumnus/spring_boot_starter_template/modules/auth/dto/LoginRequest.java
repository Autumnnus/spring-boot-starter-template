package com.autumnus.spring_boot_starter_template.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "emailOrUsername is required")
        String emailOrUsername,

        @NotBlank(message = "password is required")
        String password
) {
}
