package com.autumnus.spring_boot_starter_template.modules.auth.service;

import com.autumnus.spring_boot_starter_template.common.security.UnauthorizedException;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.*;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Legacy authentication service - DEPRECATED
 * Authentication is now handled by Keycloak.
 * This service is kept for backward compatibility only.
 *
 * @deprecated Use Keycloak OAuth2 authentication instead via /api/v1/auth/login
 */
@Service
@Transactional
@Deprecated
public class AuthServiceImpl implements AuthService {

    @Override
    @Deprecated
    public UserResponse register(RegisterRequest request) {
        throw new UnsupportedOperationException(
                "Legacy registration is deprecated. Please register through Keycloak at /oauth2/authorization/keycloak"
        );
    }

    @Override
    @Deprecated
    public TokenResponse login(LoginRequest request) {
        throw new UnsupportedOperationException(
                "Legacy JWT authentication is deprecated. Please use Keycloak OAuth2 authentication via /oauth2/authorization/keycloak"
        );
    }

    @Override
    @Deprecated
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        throw new UnsupportedOperationException(
                "Legacy token refresh is deprecated. Keycloak handles token refresh automatically"
        );
    }

    @Override
    @Deprecated
    public void logout(RefreshTokenRequest request) {
        throw new UnsupportedOperationException(
                "Legacy logout is deprecated. Use Keycloak logout via /api/v1/auth/logout"
        );
    }

    @Override
    @Deprecated
    public void verifyEmail(String token) {
        throw new UnsupportedOperationException(
                "Email verification is now handled by Keycloak"
        );
    }

    @Override
    @Deprecated
    public void requestPasswordReset(PasswordResetRequest request) {
        throw new UnsupportedOperationException(
                "Password reset is now handled by Keycloak"
        );
    }

    @Override
    @Deprecated
    public void resetPassword(ResetPasswordRequest request) {
        throw new UnsupportedOperationException(
                "Password reset is now handled by Keycloak"
        );
    }

    @Override
    @Deprecated
    public void changePassword(Long userId, ChangePasswordRequest request) {
        throw new UnsupportedOperationException(
                "Password change is now handled by Keycloak"
        );
    }
}
