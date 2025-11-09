package com.autumnus.spring_boot_starter_template.modules.users.dto;

import com.autumnus.spring_boot_starter_template.common.api.dto.BaseDto;
import com.autumnus.spring_boot_starter_template.common.storage.dto.MediaResourceResponse;
import com.autumnus.spring_boot_starter_template.modules.users.entity.RoleName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.Set;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserResponse extends BaseDto {

    private String email;
    private String username;
    private boolean active;
    private boolean emailVerified;
    private Set<RoleName> roles;
    private Instant lastLoginAt;
    private Instant passwordChangedAt;
    private Integer failedLoginAttempts;
    private Instant lockedUntil;
    private MediaResourceResponse profilePhoto;
}
