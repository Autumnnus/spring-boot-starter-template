package com.autumnus.spring_boot_starter_template.modules.auth.controller;

import com.autumnus.spring_boot_starter_template.common.api.ApiResponse;
import com.autumnus.spring_boot_starter_template.common.context.RequestContextHolder;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.*;
import com.autumnus.spring_boot_starter_template.modules.auth.service.AuthService;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Legacy authentication controller - DEPRECATED
 * Use KeycloakAuthController and Keycloak OAuth2 flows instead.
 *
 * @deprecated Authentication is now handled by Keycloak
 */
@RestController
@Deprecated
public class AuthController implements AuthApi {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        final UserResponse response = authService.register(request);
        final ApiResponse<UserResponse> payload = ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), response);
        return ResponseEntity.status(201).body(payload);
    }

    @Override
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        final TokenResponse response = authService.login(request);
        final ApiResponse<TokenResponse> payload = ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), response);
        return ResponseEntity.ok(payload);
    }

    @Override
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        final TokenResponse response = authService.refreshToken(request);
        final ApiResponse<TokenResponse> payload = ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), response);
        return ResponseEntity.ok(payload);
    }

    @Override
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> verifyEmail(String token) {
        authService.verifyEmail(token);
        final ApiResponse<Void> payload = ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), null);
        return ResponseEntity.ok(payload);
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request);
        final ApiResponse<Void> payload = ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), null);
        return ResponseEntity.accepted().body(payload);
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        final ApiResponse<Void> payload = ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), null);
        return ResponseEntity.ok(payload);
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(principal.getUserId(), request);
        final ApiResponse<Void> payload = ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), null);
        return ResponseEntity.ok(payload);
    }
}
