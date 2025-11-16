package com.autumnus.spring_boot_starter_template.modules.auth.controller;

import com.autumnus.spring_boot_starter_template.common.api.ApiResponse;
import com.autumnus.spring_boot_starter_template.common.config.KeycloakProperties;
import com.autumnus.spring_boot_starter_template.common.security.KeycloakUserService;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import com.autumnus.spring_boot_starter_template.modules.users.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication (Keycloak)", description = "Keycloak-based authentication endpoints")
@Slf4j
public class KeycloakAuthController {

    private final KeycloakUserService keycloakUserService;
    private final KeycloakProperties keycloakProperties;
    private final UserMapper userMapper;

    public KeycloakAuthController(
            KeycloakUserService keycloakUserService,
            KeycloakProperties keycloakProperties,
            UserMapper userMapper
    ) {
        this.keycloakUserService = keycloakUserService;
        this.keycloakProperties = keycloakProperties;
        this.userMapper = userMapper;
    }

    @GetMapping("/login")
    @Operation(summary = "Redirect to Keycloak login page")
    public RedirectView login() {
        return new RedirectView("/oauth2/authorization/keycloak");
    }

    @GetMapping("/logout")
    @Operation(summary = "Logout from Keycloak")
    public RedirectView logout(HttpServletRequest request) {
        String logoutUrl = String.format(
                "%s/realms/%s/protocol/openid-connect/logout?redirect_uri=%s",
                keycloakProperties.getAuthServerUrl(),
                keycloakProperties.getRealm(),
                request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
        );
        return new RedirectView(logoutUrl);
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current user information",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        User user = keycloakUserService.getOrCreateUserFromToken();
        UserResponse response = userMapper.toResponse(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/userinfo")
    @Operation(
            summary = "Get Keycloak user information",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserInfo(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sub", jwt.getSubject());
        userInfo.put("email", jwt.getClaim("email"));
        userInfo.put("email_verified", jwt.getClaim("email_verified"));
        userInfo.put("preferred_username", jwt.getClaim("preferred_username"));
        userInfo.put("given_name", jwt.getClaim("given_name"));
        userInfo.put("family_name", jwt.getClaim("family_name"));
        userInfo.put("name", jwt.getClaim("name"));
        userInfo.put("roles", jwt.getClaim("realm_access"));

        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    @GetMapping("/oauth2/callback")
    @Operation(summary = "OAuth2 callback endpoint (handled by Spring Security)")
    public RedirectView oauth2Callback() {
        return new RedirectView("/swagger-ui/index.html");
    }
}
