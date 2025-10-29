package com.autumnus.spring_boot_starter_template.modules.users.mapper;

import com.autumnus.spring_boot_starter_template.modules.users.dto.UserCreateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserUpdateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.entity.Role;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserStatus;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserCreateRequest request) {
        final User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setDisplayName(request.displayName());
        user.setPasswordHash(request.password());
        user.setStatus(request.status() != null ? request.status() : UserStatus.ACTIVE);
        user.setActive(user.getStatus() == UserStatus.ACTIVE);
        return user;
    }

    public void updateEntity(UserUpdateRequest request, User user) {
        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }
        if (request.status() != null) {
            user.setStatus(request.status());
            user.setActive(request.status() == UserStatus.ACTIVE);
        }
        if (request.username() != null) {
            user.setUsername(request.username());
        }
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getUuid())
                .email(user.getEmail())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .status(user.getStatus())
                .active(user.isActive())
                .emailVerified(user.isEmailVerified())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .lockedUntil(user.getLockedUntil())
                .roles(mapRoles(user.getRoles()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private List<String> mapRoles(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }
}
