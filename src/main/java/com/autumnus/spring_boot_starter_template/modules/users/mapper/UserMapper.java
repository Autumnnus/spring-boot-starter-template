package com.autumnus.spring_boot_starter_template.modules.users.mapper;

import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserUpdateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.entity.RoleName;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserRoleAssignment;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

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
                .build();
    }

    public Set<RoleName> extractRoleNames(User user) {
        return user.getRoleAssignments()
                .stream()
                .map(UserRoleAssignment::getRole)
                .map(role -> role.getName())
                .collect(Collectors.toUnmodifiableSet());
    }
}
