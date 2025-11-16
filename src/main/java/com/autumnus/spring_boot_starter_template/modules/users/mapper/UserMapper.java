package com.autumnus.spring_boot_starter_template.modules.users.mapper;

import com.autumnus.spring_boot_starter_template.common.storage.dto.MediaFileResponse;
import com.autumnus.spring_boot_starter_template.common.storage.dto.MediaResourceResponse;
import com.autumnus.spring_boot_starter_template.common.storage.model.MediaFileDescriptor;
import com.autumnus.spring_boot_starter_template.common.storage.model.MediaManifest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserUpdateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper for User entity to DTOs.
 * Handles only application-specific user data as authentication is managed by Keycloak.
 */
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

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .keycloakUserId(user.getKeycloakUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .active(user.isActive())
                .lastLoginAt(user.getLastLoginAt())
                .profilePhoto(mapProfilePhoto(user))
                .build();
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
