package com.autumnus.spring_boot_starter_template.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Email(message = "must be a valid email")
        String email,

        @Size(min = 3, max = 50, message = "username must be between 3 and 50 characters")
        String username,

        String displayName
) {
}
