package com.autumnus.spring_boot_starter_template.modules.users.dto;

import jakarta.validation.constraints.Email;

import java.util.Set;

public record UserUpdateRequest(
        @Email(message = "must be a valid email")
        String email,

        String username,

        Boolean active


) {
}
