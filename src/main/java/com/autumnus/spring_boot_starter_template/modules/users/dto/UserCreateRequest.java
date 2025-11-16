package com.autumnus.spring_boot_starter_template.modules.users.dto;

import com.autumnus.spring_boot_starter_template.modules.users.entity.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.Set;

/**
 * User creation request DTO.
 * Note: Password and roles are deprecated as authentication is handled by Keycloak.
 * This DTO is kept for backward compatibility and admin operations.
 */
@Builder
public record UserCreateRequest(
        @Email(message = "must be a valid email")
        @NotBlank(message = "email is required")
        String email,

        @NotBlank(message = "username is required")
        String username,

        @Deprecated
        String password,

        @Deprecated
        Set<RoleName> roles,

        Boolean active
) {
}
