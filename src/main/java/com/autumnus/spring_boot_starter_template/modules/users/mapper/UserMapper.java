package com.autumnus.spring_boot_starter_template.modules.users.mapper;

import com.autumnus.spring_boot_starter_template.modules.users.dto.UserCreateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserUpdateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import java.util.HashSet;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final ModelMapper modelMapper;

    public UserMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public User toEntity(UserCreateRequest request) {
        final User user = modelMapper.map(request, User.class);
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        if (request.roles() != null) {
            user.setRoles(new HashSet<>(request.roles()));
        }
        return user;
    }

    public void updateEntity(UserUpdateRequest request, User user) {
        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }
        if (request.status() != null) {
            user.setStatus(request.status());
        }
        if (request.roles() != null) {
            user.setRoles(new HashSet<>(request.roles()));
        }
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .status(user.getStatus())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
