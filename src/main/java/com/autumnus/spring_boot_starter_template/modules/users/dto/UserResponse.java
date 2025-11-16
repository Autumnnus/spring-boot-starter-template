package com.autumnus.spring_boot_starter_template.modules.users.dto;

import com.autumnus.spring_boot_starter_template.common.api.dto.BaseDto;
import com.autumnus.spring_boot_starter_template.common.storage.dto.MediaResourceResponse;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * User response DTO for application-specific user data.
 * Authentication and role information is managed by Keycloak.
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserResponse extends BaseDto {

    private String keycloakUserId;
    private String email;
    private String username;
    private boolean active;
    private Instant lastLoginAt;
    private MediaResourceResponse profilePhoto;
}
