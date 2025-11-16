package com.autumnus.spring_boot_starter_template.modules.auth.service;

import com.autumnus.spring_boot_starter_template.common.config.KeycloakProperties;
import com.autumnus.spring_boot_starter_template.common.config.SecurityProperties;
import com.autumnus.spring_boot_starter_template.common.logging.annotation.AuditAction;
import com.autumnus.spring_boot_starter_template.common.logging.annotation.Auditable;
import com.autumnus.spring_boot_starter_template.common.logging.annotation.NoLog;
import com.autumnus.spring_boot_starter_template.common.logging.context.AuditContextHolder;
import com.autumnus.spring_boot_starter_template.common.security.JwtTokenProvider;
import com.autumnus.spring_boot_starter_template.common.security.KeycloakService;
import com.autumnus.spring_boot_starter_template.common.security.KeycloakTokenService;
import com.autumnus.spring_boot_starter_template.common.security.UnauthorizedException;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.*;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserCreateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.entity.*;
import com.autumnus.spring_boot_starter_template.modules.users.repository.EmailVerificationTokenRepository;
import com.autumnus.spring_boot_starter_template.modules.users.repository.PasswordResetTokenRepository;
import com.autumnus.spring_boot_starter_template.modules.users.repository.UserRepository;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserDetailsServiceImpl;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserService;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserServiceValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final SecurityProperties securityProperties;
    private final PasswordEncoder passwordEncoder;
    private final KeycloakProperties keycloakProperties;

    @Autowired(required = false)
    private KeycloakService keycloakService;

    @Autowired(required = false)
    private KeycloakTokenService keycloakTokenService;

    public AuthServiceImpl(
            UserService userService,
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            UserDetailsServiceImpl userDetailsService,
            JwtTokenProvider jwtTokenProvider,
            TokenService tokenService,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            SecurityProperties securityProperties,
            PasswordEncoder passwordEncoder,
            KeycloakProperties keycloakProperties
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenService = tokenService;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.securityProperties = securityProperties;
        this.passwordEncoder = passwordEncoder;
        this.keycloakProperties = keycloakProperties;
    }

    private boolean isKeycloakEnabled() {
        return keycloakProperties.isEnabled() && keycloakService != null && keycloakTokenService != null;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        if (isKeycloakEnabled()) {
            return registerWithKeycloak(request);
        } else {
            return registerLegacy(request);
        }
    }

    private UserResponse registerLegacy(RegisterRequest request) {
        final UserResponse response = userService.createUser(UserCreateRequest.builder()
                .email(request.email())
                .username(request.username())
                .password(request.password())
                .roles(EnumSet.of(RoleName.USER))
                .active(true)
                .build());
        userService.findEntityByEmail(request.email()).ifPresent(this::createEmailVerificationToken);
        return response;
    }

    private UserResponse registerWithKeycloak(RegisterRequest request) {
        // Create user in Keycloak first
        log.info("Creating user in Keycloak: {}", request.email());
        String keycloakId = keycloakService.createUser(
                request.username(),
                request.email(),
                request.password(),
                null, // firstName
                null  // lastName
        );

        // Assign default roles in Keycloak
        keycloakService.assignRolesToUser(keycloakId, Collections.singletonList("USER"));

        // Create user in local database with Keycloak ID
        log.info("Creating user in local database with Keycloak ID: {}", keycloakId);
        User user = new User();
        user.setKeycloakId(keycloakId);
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setActive(true);
        user.setEmailVerified(false);
        user.setPasswordHash(null); // Password managed by Keycloak
        user.setPasswordChangedAt(Instant.now());

        user = userRepository.save(user);

        log.info("User registered successfully with Keycloak ID: {}", keycloakId);

        // Return user response
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .active(user.isActive())
                .emailVerified(user.isEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(EnumSet.of(RoleName.USER))
                .build();
    }

    @Override
    @Auditable(entityType = "USER", action = AuditAction.LOGIN, entityIdExpression = "#request.email")
    public TokenResponse login(LoginRequest request) {
        if (isKeycloakEnabled()) {
            return loginWithKeycloak(request);
        } else {
            return loginLegacy(request);
        }
    }

    private TokenResponse loginLegacy(LoginRequest request) {
        final User user = userService.findEntityByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        AuditContextHolder.setEntityId(user.getId().toString());
        userService.checkAccountLocked(user);
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException ex) {
            userService.incrementFailedAttempts(user);
            throw new UnauthorizedException("Invalid credentials");
        }
        userService.resetFailedAttempts(user);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        final String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        final RefreshToken refreshToken = tokenService.createRefreshToken(user, request.deviceInfo(), request.ipAddress());
        return TokenResponse.builder()
                .accessToken(accessToken)
                .accessTokenExpiresAt(Instant.now().plus(securityProperties.getAccessTokenTtl()))
                .refreshToken(refreshToken.getToken())
                .refreshTokenExpiresAt(refreshToken.getExpiresAt())
                .tokenType("Bearer")
                .build();
    }

    private TokenResponse loginWithKeycloak(LoginRequest request) {
        // Find user in local database
        final User user = userService.findEntityByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        AuditContextHolder.setEntityId(user.getId().toString());

        try {
            // Obtain token from Keycloak
            log.info("Obtaining token from Keycloak for user: {}", request.email());
            Map<String, Object> tokenResponse = keycloakTokenService.obtainToken(request.email(), request.password());

            // Update user last login
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);

            log.info("Login successful for user: {}", request.email());

            // Return Keycloak tokens
            return TokenResponse.builder()
                    .accessToken((String) tokenResponse.get("access_token"))
                    .accessTokenExpiresAt(Instant.now().plusSeconds((Integer) tokenResponse.get("expires_in")))
                    .refreshToken((String) tokenResponse.get("refresh_token"))
                    .refreshTokenExpiresAt(Instant.now().plusSeconds((Integer) tokenResponse.get("refresh_expires_in")))
                    .tokenType((String) tokenResponse.get("token_type"))
                    .build();
        } catch (Exception ex) {
            log.error("Login failed for user: {}", request.email(), ex);
            throw new UnauthorizedException("Invalid credentials");
        }
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        if (isKeycloakEnabled()) {
            return refreshTokenWithKeycloak(request);
        } else {
            return refreshTokenLegacy(request);
        }
    }

    private TokenResponse refreshTokenLegacy(RefreshTokenRequest request) {
        final RefreshToken refreshToken = tokenService.validateRefreshToken(request.refreshToken());
        final Long userId = jwtTokenProvider.extractUserId(refreshToken.getToken());
        final User user = userService.findEntityById(userId);
        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        final String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        tokenService.revokeToken(refreshToken.getToken());
        final RefreshToken rotated = tokenService.createRefreshToken(user, refreshToken.getDeviceInfo(), refreshToken.getIpAddress());
        return TokenResponse.builder()
                .accessToken(accessToken)
                .accessTokenExpiresAt(Instant.now().plus(securityProperties.getAccessTokenTtl()))
                .refreshToken(rotated.getToken())
                .refreshTokenExpiresAt(rotated.getExpiresAt())
                .tokenType("Bearer")
                .build();
    }

    private TokenResponse refreshTokenWithKeycloak(RefreshTokenRequest request) {
        try {
            log.info("Refreshing token with Keycloak");
            Map<String, Object> tokenResponse = keycloakTokenService.refreshToken(request.refreshToken());

            return TokenResponse.builder()
                    .accessToken((String) tokenResponse.get("access_token"))
                    .accessTokenExpiresAt(Instant.now().plusSeconds((Integer) tokenResponse.get("expires_in")))
                    .refreshToken((String) tokenResponse.get("refresh_token"))
                    .refreshTokenExpiresAt(Instant.now().plusSeconds((Integer) tokenResponse.get("refresh_expires_in")))
                    .tokenType((String) tokenResponse.get("token_type"))
                    .build();
        } catch (Exception ex) {
            log.error("Token refresh failed", ex);
            throw new UnauthorizedException("Invalid refresh token");
        }
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        if (isKeycloakEnabled()) {
            logoutWithKeycloak(request);
        } else {
            logoutLegacy(request);
        }
    }

    private void logoutLegacy(RefreshTokenRequest request) {
        tokenService.revokeToken(request.refreshToken());
    }

    private void logoutWithKeycloak(RefreshTokenRequest request) {
        try {
            log.info("Logging out from Keycloak");
            keycloakTokenService.logout(request.refreshToken());
        } catch (Exception ex) {
            log.error("Logout failed", ex);
            // Don't throw exception on logout failure
        }
    }

    @Override
    public void verifyEmail(String token) {
        final EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenValidationException("VERIFICATION_TOKEN_NOT_FOUND", "Verification token not found"));
        if (verificationToken.isUsed()) {
            throw new TokenValidationException("VERIFICATION_TOKEN_USED", "Verification token already used");
        }
        if (verificationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenValidationException("VERIFICATION_TOKEN_EXPIRED", "Verification token expired");
        }
        final User user = verificationToken.getUser();
        user.setEmailVerified(true);
        verificationToken.setUsed(true);
        verificationToken.setUsedAt(Instant.now());
        userRepository.save(user);
        emailVerificationTokenRepository.save(verificationToken);
    }

    @Override
    public void requestPasswordReset(PasswordResetRequest request) {
        final User user = userService.findEntityByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("No account associated with this email"));
        final PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(Instant.now().plus(securityProperties.getPasswordResetTokenTtl()));
        passwordResetTokenRepository.save(token);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        final PasswordResetToken token = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new TokenValidationException("PASSWORD_TOKEN_NOT_FOUND", "Password reset token not found"));
        if (token.isUsed()) {
            throw new TokenValidationException("PASSWORD_TOKEN_USED", "Password reset token already used");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenValidationException("PASSWORD_TOKEN_EXPIRED", "Password reset token expired");
        }
        final User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(Instant.now());
        token.setUsed(true);
        token.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(token);
        userRepository.save(user);
        tokenService.revokeAllUserTokens(user.getId());
    }

    @Override
    @NoLog
    public void changePassword(Long userId, ChangePasswordRequest request) {
        final User user = userService.findEntityById(userId);
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new UserServiceValidationException("INVALID_OLD_PASSWORD", "Existing password does not match");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);
        tokenService.revokeAllUserTokens(user.getId());
    }

    private void createEmailVerificationToken(User user) {
        final EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(Instant.now().plus(securityProperties.getEmailVerificationTokenTtl()));
        emailVerificationTokenRepository.save(token);
    }
}
