package com.autumnus.spring_boot_starter_template.modules.users.mapper;

import com.autumnus.spring_boot_starter_template.common.storage.dto.MediaFileResponse;
import com.autumnus.spring_boot_starter_template.common.storage.dto.MediaResourceResponse;
import com.autumnus.spring_boot_starter_template.common.storage.model.MediaFileDescriptor;
import com.autumnus.spring_boot_starter_template.common.storage.model.MediaManifest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserUpdateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.entity.RoleName;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserRoleAssignment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    private static final Logger log = LoggerFactory.getLogger(UserMapper.class);

    private final ObjectMapper objectMapper;

    public UserMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void updateEntity(UserUpdateRequest request, User user) {
        if (request.email() != null) {
            user.setEmail(request.email());
        }
        if (request.username() != null) {
            user.setUsername(request.username());
        }
        if (request.active() != null) {
            user.setActive(request.active());
        }
    }

    public UserResponse toResponse(User user, Set<RoleName> roles) {
        return UserResponse.builder()
                .uuid(user.getUuid())
                .email(user.getEmail())
                .username(user.getUsername())
                .active(user.isActive())
                .emailVerified(user.isEmailVerified())
                .roles(roles)
                .lastLoginAt(user.getLastLoginAt())
                .passwordChangedAt(user.getPasswordChangedAt())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .lockedUntil(user.getLockedUntil())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .profilePhoto(mapProfilePhoto(user))
                .build();
    }

    public Set<RoleName> extractRoleNames(User user) {
        return user.getRoleAssignments()
                .stream()
                .map(UserRoleAssignment::getRole)
                .map(role -> role.getName())
                .collect(Collectors.toUnmodifiableSet());
    }

    private MediaResourceResponse mapProfilePhoto(User user) {
        if (user.getProfilePhotoManifest() == null) {
            return null;
        }
        try {
            final MediaManifest manifest = objectMapper.readValue(user.getProfilePhotoManifest(), MediaManifest.class);
            final MediaFileResponse original = toResponse(manifest.original());
            return new MediaResourceResponse(
                    original,
                    manifest.variants() == null ? null : manifest.variants().entrySet().stream()
                            .collect(Collectors.toMap(
                                    entry -> entry.getKey().name().toLowerCase(),
                                    entry -> toResponse(entry.getValue()),
                                    (left, right) -> left,
                                    java.util.LinkedHashMap::new
                            ))
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to parse profile photo manifest for user {}", user.getId(), e);
            throw new IllegalStateException("Profile photo manifest is corrupted", e);
        }
    }

    private MediaFileResponse toResponse(MediaFileDescriptor descriptor) {
        if (descriptor == null) {
            return null;
        }
        return new MediaFileResponse(descriptor.url(), descriptor.contentType(), descriptor.size());
    }
}
