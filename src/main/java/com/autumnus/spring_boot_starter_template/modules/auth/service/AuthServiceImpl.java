package com.autumnus.spring_boot_starter_template.modules.auth.service;

import com.autumnus.spring_boot_starter_template.modules.auth.dto.LoginRequest;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.RegisterRequest;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.TokenResponse;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.UpdateProfileRequest;
import com.autumnus.spring_boot_starter_template.modules.auth.entity.EmailVerificationToken;
import com.autumnus.spring_boot_starter_template.modules.auth.entity.PasswordResetToken;
import com.autumnus.spring_boot_starter_template.modules.auth.entity.RefreshToken;
import com.autumnus.spring_boot_starter_template.modules.auth.repository.EmailVerificationTokenRepository;
import com.autumnus.spring_boot_starter_template.modules.auth.repository.PasswordResetTokenRepository;
import com.autumnus.spring_boot_starter_template.common.config.SecurityProperties;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.entity.Role;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import com.autumnus.spring_boot_starter_template.modules.users.repository.RoleRepository;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserAlreadyExistsException;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserService;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final DatabaseUserDetailsService userDetailsService;
    private final SecurityProperties securityProperties;

    public AuthServiceImpl(
            UserService userService,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            TokenService tokenService,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            DatabaseUserDetailsService userDetailsService,
            SecurityProperties securityProperties
    ) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenService = tokenService;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userDetailsService = userDetailsService;
        this.securityProperties = securityProperties;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        userService.findByEmail(request.email()).ifPresent(user -> {
            throw new UserAlreadyExistsException("Email is already in use");
        });
        userService.findByUsername(request.username()).ifPresent(user -> {
            throw new UserAlreadyExistsException("Username is already in use");
        });

        final Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Default USER role not configured"));

        final UserResponse saved = userService.createUser(
                com.autumnus.spring_boot_starter_template.modules.users.dto.UserCreateRequest.builder()
                        .email(request.email())
                        .username(request.username())
                        .password(request.password())
                        .displayName(request.displayName())
                        .status(com.autumnus.spring_boot_starter_template.modules.users.entity.UserStatus.ACTIVE)
                        .roles(Set.of(userRole.getName()))
                        .build()
        );

        final EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(userService.findByEmail(saved.getEmail()).orElseThrow());
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        emailVerificationTokenRepository.save(token);

        return saved;
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        final Optional<User> candidate = userService.findByEmail(request.emailOrUsername())
                .or(() -> userService.findByUsername(request.emailOrUsername()));
        final User user = candidate.orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        userService.checkAccountLocked(user);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            userService.incrementFailedAttempts(user);
            throw new IllegalArgumentException("Invalid credentials");
        }
        user.setLastLoginAt(Instant.now());
        userService.resetFailedAttempts(user);

        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), request.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        final var userDetails = userDetailsService.loadUserById(user.getId());
        final String accessToken = jwtService.generateAccessToken(userDetails);
        final RefreshToken refreshToken = tokenService.createRefreshToken(user, null, null);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .accessTokenExpiresAt(Instant.now().plus(securityProperties.getAccessTokenTtl()))
                .refreshToken(refreshToken.getToken())
                .refreshTokenExpiresAt(refreshToken.getExpiresAt())
                .build();
    }

    @Override
    public TokenResponse refreshToken(String refreshTokenValue) {
        final RefreshToken refreshToken = tokenService.validateRefreshToken(refreshTokenValue);
        final var userDetails = userDetailsService.loadUserById(refreshToken.getUser().getId());
        final String accessToken = jwtService.generateAccessToken(userDetails);
        final RefreshToken newRefreshToken = tokenService.createRefreshToken(refreshToken.getUser(), null, null);
        tokenService.revokeToken(refreshTokenValue);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .accessTokenExpiresAt(Instant.now().plus(securityProperties.getAccessTokenTtl()))
                .refreshToken(newRefreshToken.getToken())
                .refreshTokenExpiresAt(newRefreshToken.getExpiresAt())
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        tokenService.revokeToken(refreshToken);
        SecurityContextHolder.clearContext();
    }

    @Override
    public void verifyEmail(String token) {
        final EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));
        if (verificationToken.isUsed() || verificationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Verification token expired");
        }
        final User user = userService.findById(verificationToken.getUser().getId());
        user.setEmailVerified(true);
        user.setActive(true);
        user.setStatus(com.autumnus.spring_boot_starter_template.modules.users.entity.UserStatus.ACTIVE);
        verificationToken.setUsed(true);
        verificationToken.setUsedAt(Instant.now());
        emailVerificationTokenRepository.save(verificationToken);
        userService.resetFailedAttempts(user);
    }

    @Override
    public void requestPasswordReset(String email) {
        final User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        final PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        passwordResetTokenRepository.save(token);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        final PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));
        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Reset token expired");
        }
        final User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(Instant.now());
        resetToken.setUsed(true);
        resetToken.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(resetToken);
        userService.resetFailedAttempts(user);
        tokenService.revokeAllUserTokens(user.getId());
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        final User user = userService.findById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(Instant.now());
        userService.resetFailedAttempts(user);
        tokenService.revokeAllUserTokens(user.getId());
    }

    @Override
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        return userService.updateProfile(userId, request);
    }
}
