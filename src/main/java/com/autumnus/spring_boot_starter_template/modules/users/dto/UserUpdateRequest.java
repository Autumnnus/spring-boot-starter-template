package com.autumnus.spring_boot_starter_template.modules.users.dto;

import com.autumnus.spring_boot_starter_template.modules.users.entity.UserRole;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record UserUpdateRequest(
        @Email(message = "must be a valid email")
        String email,

        String displayName,

        UserStatus status,

        Set<UserRole> roles
) {
}
