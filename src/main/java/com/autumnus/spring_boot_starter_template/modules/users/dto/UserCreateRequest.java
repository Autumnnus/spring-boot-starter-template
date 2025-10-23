package com.autumnus.spring_boot_starter_template.modules.users.dto;

import com.autumnus.spring_boot_starter_template.modules.users.entity.UserRole;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record UserCreateRequest(
        @Email(message = "must be a valid email")
        @NotBlank(message = "email is required")
        String email,

        @NotBlank(message = "displayName is required")
        String displayName,

        @NotNull(message = "status is required")
        UserStatus status,

        Set<UserRole> roles
) {
}
