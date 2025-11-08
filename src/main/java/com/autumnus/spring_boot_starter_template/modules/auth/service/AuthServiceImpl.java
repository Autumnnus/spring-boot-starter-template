package com.autumnus.spring_boot_starter_template.modules.auth.service;

import com.autumnus.spring_boot_starter_template.common.config.SecurityProperties;
import com.autumnus.spring_boot_starter_template.common.logging.annotation.AuditAction;
import com.autumnus.spring_boot_starter_template.common.logging.annotation.Auditable;
import com.autumnus.spring_boot_starter_template.common.logging.annotation.NoLog;
import com.autumnus.spring_boot_starter_template.common.logging.context.AuditContextHolder;
import com.autumnus.spring_boot_starter_template.common.security.JwtTokenProvider;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

@Service
@Transactional
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
            PasswordEncoder passwordEncoder
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
    }

    @Override
    public UserResponse register(RegisterRequest request) {
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

    @Override
    @Auditable(entityType = "USER", action = AuditAction.LOGIN, entityIdExpression = "#request.email")
    public TokenResponse login(LoginRequest request) {
        final User user = userService.findEntityByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        AuditContextHolder.setEntityId(user.getUuid().toString());
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

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest request) {
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

    @Override
    public void logout(RefreshTokenRequest request) {
        tokenService.revokeToken(request.refreshToken());
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
