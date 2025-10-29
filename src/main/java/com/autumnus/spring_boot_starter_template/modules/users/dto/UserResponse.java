package com.autumnus.spring_boot_starter_template.modules.users.dto;

import com.autumnus.spring_boot_starter_template.modules.users.entity.RoleName;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class UserResponse {

    private final UUID uuid;
    private final String email;
    private final String username;
    private final boolean active;
    private final boolean emailVerified;
    private final Set<RoleName> roles;
    private final Instant lastLoginAt;
    private final Instant passwordChangedAt;
    private final Integer failedLoginAttempts;
    private final Instant lockedUntil;
    private final Instant createdAt;
    private final Instant updatedAt;
}
