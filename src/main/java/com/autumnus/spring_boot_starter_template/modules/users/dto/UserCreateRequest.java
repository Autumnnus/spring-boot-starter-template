package com.autumnus.spring_boot_starter_template.modules.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.Set;

@Builder
public record UserCreateRequest(
        @Email(message = "must be a valid email")
        @NotBlank(message = "email is required")
        String email,

        @NotBlank(message = "username is required")
        String username,

        @NotBlank(message = "password is required")
        String password,



        Boolean active
) {
}
