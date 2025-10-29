package com.autumnus.spring_boot_starter_template.modules.auth.service;

import com.autumnus.spring_boot_starter_template.modules.auth.dto.LoginRequest;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.RegisterRequest;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.TokenResponse;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.UpdateProfileRequest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refreshToken(String refreshToken);

    void logout(String refreshToken);

    void verifyEmail(String token);

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);

    void changePassword(Long userId, String oldPassword, String newPassword);

    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
}
