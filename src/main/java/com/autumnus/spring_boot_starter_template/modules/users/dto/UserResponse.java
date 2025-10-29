package com.autumnus.spring_boot_starter_template.modules.users.dto;

import com.autumnus.spring_boot_starter_template.modules.users.entity.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class UserResponse {

    private final UUID id;
    private final String email;
    private final String username;
    private final String displayName;
    private final UserStatus status;
    private final boolean active;
    private final boolean emailVerified;
    private final int failedLoginAttempts;
    private final Instant lockedUntil;
    private final List<String> roles;
    private final Instant createdAt;
    private final Instant updatedAt;
}
