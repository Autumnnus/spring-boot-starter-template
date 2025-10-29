package com.autumnus.spring_boot_starter_template.modules.auth.controller;

import com.autumnus.spring_boot_starter_template.common.api.ApiResponse;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.*;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
@RequestMapping("/api/v1/auth")
public interface AuthApi {

    @Operation(summary = "Register", description = "Create a new user account")
    @PostMapping("/register")
    ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request);

    @Operation(summary = "Login", description = "Authenticate a user and issue tokens")
    @PostMapping("/login")
    ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request);

    @Operation(summary = "Refresh token", description = "Exchange a refresh token for new tokens")
    @PostMapping("/refresh")
    ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request);

    @Operation(summary = "Logout", description = "Revoke a refresh token")
    @PostMapping("/logout")
    ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request);

    @Operation(summary = "Verify email", description = "Verify a user's email address")
    @GetMapping("/verify-email")
    ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token);

    @Operation(summary = "Request password reset", description = "Initiate a password reset flow")
    @PostMapping("/request-password-reset")
    ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request);

    @Operation(summary = "Reset password", description = "Reset password using a token")
    @PostMapping("/reset-password")
    ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request);

    @Operation(summary = "Change password", description = "Change the password for the authenticated user")
    @PostMapping("/change-password")
    ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request
    );
}
