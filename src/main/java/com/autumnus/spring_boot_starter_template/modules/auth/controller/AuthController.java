package com.autumnus.spring_boot_starter_template.modules.auth.controller;

import com.autumnus.spring_boot_starter_template.common.api.ApiResponse;
import com.autumnus.spring_boot_starter_template.common.context.RequestContextHolder;
import com.autumnus.spring_boot_starter_template.common.security.UserPrincipal;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.ChangePasswordRequest;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.LoginRequest;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.PasswordResetRequest;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.RefreshTokenRequest;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.RegisterRequest;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.ResetPasswordRequest;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.TokenResponse;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.UpdateProfileRequest;
import com.autumnus.spring_boot_starter_template.modules.auth.service.AuthService;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        final UserResponse response = authService.register(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), response));
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        final TokenResponse response = authService.login(request);
        return ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), response);
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        final TokenResponse response = authService.refreshToken(request.refreshToken());
        return ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/request-reset")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/change")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        if (principal == null) {
            throw new IllegalArgumentException("User is not authenticated");
        }
        authService.changePassword(principal.getUserId(), request.oldPassword(), request.newPassword());
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/profile")
    public ApiResponse<UserResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        if (principal == null) {
            throw new IllegalArgumentException("User is not authenticated");
        }
        final UserResponse response = authService.updateProfile(principal.getUserId(), request);
        return ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), response);
    }
}
