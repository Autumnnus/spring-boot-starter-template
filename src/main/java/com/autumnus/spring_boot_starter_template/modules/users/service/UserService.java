package com.autumnus.spring_boot_starter_template.modules.users.service;

import com.autumnus.spring_boot_starter_template.modules.users.dto.ProfilePhotoUploadCommand;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UpdateProfileRequest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserCreateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserUpdateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
public interface UserService {

    Page<UserResponse> listUsers(Pageable pageable, Boolean active);

    UserResponse getUser(Long id);

    UserResponse createUser(UserCreateRequest request);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    Optional<User> findEntityByEmail(String email);

    Optional<User> findEntityByUsername(String username);

    User findEntityById(Long id);

    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    UserResponse updateProfilePhoto(Long id, ProfilePhotoUploadCommand command);

    void removeProfilePhoto(Long id);

    void activateUser(Long userId);

    void deactivateUser(Long userId);

    void checkAccountLocked(User user);

    void incrementFailedAttempts(User user);

    void resetFailedAttempts(User user);
}
