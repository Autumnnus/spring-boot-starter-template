package com.autumnus.spring_boot_starter_template.modules.users.dto;

import java.util.Set;

import com.autumnus.spring_boot_starter_template.modules.users.entity.RoleName;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserCreateRequest(
        @Email(message = "must be a valid email")
        @NotBlank(message = "email is required")
        String email,

        @NotBlank(message = "username is required")
        String username,

        @NotBlank(message = "password is required")
        String password,

        Set<RoleName> roles,

        Boolean active,

        String preferredLanguage
) {
}
