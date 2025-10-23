package com.autumnus.spring_boot_starter_template.modules.users.dto;

import com.autumnus.spring_boot_starter_template.modules.users.entity.UserRole;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserStatus;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private final UUID id;
    private final String email;
    private final String displayName;
    private final UserStatus status;
    private final Set<UserRole> roles;
    private final Instant createdAt;
    private final Instant updatedAt;
}
