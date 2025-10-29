package com.autumnus.spring_boot_starter_template.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email(message = "must be a valid email")
        @NotBlank(message = "email is required")
        String email,

        @NotBlank(message = "username is required")
        String username,

        @NotBlank(message = "password is required")
        @Size(min = 8, message = "password must be at least 8 characters")
        String password
) {
}
