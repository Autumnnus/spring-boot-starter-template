package com.autumnus.spring_boot_starter_template.modules.auth.service;

import com.autumnus.spring_boot_starter_template.modules.auth.dto.*;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    void verifyEmail(String token);

    void requestPasswordReset(PasswordResetRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);
}
