package com.autumnus.spring_boot_starter_template.modules.users.dto;

import com.autumnus.spring_boot_starter_template.modules.users.entity.RoleName;
import jakarta.validation.constraints.Email;

import java.util.Set;

public record UserUpdateRequest(
        @Email(message = "must be a valid email")
        String email,

        String username,

        Boolean active,

        Set<RoleName> roles
) {
}
