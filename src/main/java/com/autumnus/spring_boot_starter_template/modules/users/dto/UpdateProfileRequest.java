package com.autumnus.spring_boot_starter_template.modules.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @Email(message = "must be a valid email")
        String email,

        @NotBlank(message = "username is required")
        String username
) {
}
