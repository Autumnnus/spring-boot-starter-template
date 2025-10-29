package com.autumnus.spring_boot_starter_template.modules.users.service;

import com.autumnus.spring_boot_starter_template.modules.users.dto.UserCreateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserUpdateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserRole;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserStatus;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.UpdateProfileRequest;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<UserResponse> listUsers(Pageable pageable, UserRole role, UserStatus status);

    UserResponse getUser(UUID id);

    UserResponse createUser(UserCreateRequest request);

    UserResponse updateUser(UUID id, UserUpdateRequest request);

    void deleteUser(UUID id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    User findById(Long id);

    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    void activateUser(Long userId);

    void deactivateUser(Long userId);

    void checkAccountLocked(User user);

    void incrementFailedAttempts(User user);

    void resetFailedAttempts(User user);
}
