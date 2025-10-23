package com.autumnus.spring_boot_starter_template.modules.users.service;

import com.autumnus.spring_boot_starter_template.modules.users.dto.UserCreateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserUpdateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserRole;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<UserResponse> listUsers(Pageable pageable, UserRole role, UserStatus status);

    UserResponse getUser(UUID id);

    UserResponse createUser(UserCreateRequest request);

    UserResponse updateUser(UUID id, UserUpdateRequest request);

    void deleteUser(UUID id);
}
