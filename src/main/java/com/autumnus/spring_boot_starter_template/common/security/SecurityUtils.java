package com.autumnus.spring_boot_starter_template.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

/**
 * Security utilities for Keycloak-based authentication.
 * Provides helper methods for accessing user information from JWT tokens.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Get current user ID from Keycloak JWT token.
     * Attempts to extract user_id claim from the JWT.
     */
    public static Optional<Long> getCurrentUserId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            return Optional.empty();
        }

        Jwt jwt = jwtAuth.getToken();
        Object userIdClaim = jwt.getClaim("user_id");

        if (userIdClaim instanceof Long userId) {
            return Optional.of(userId);
        }

        if (userIdClaim instanceof String userIdStr) {
            try {
                return Optional.of(Long.parseLong(userIdStr));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    /**
     * Get Keycloak user ID (sub claim) from JWT
     */
    public static Optional<String> getKeycloakUserId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return Optional.ofNullable(jwt.getSubject());
        }
        return Optional.empty();
    }

    /**
     * Get current user email from Keycloak JWT
     */
    public static Optional<String> getCurrentUserEmail() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return Optional.ofNullable(jwt.getClaim("email"));
        }
        return Optional.empty();
    }

    /**
     * Check if current user has a specific role.
     * Supports both ROLE_ prefixed and non-prefixed role names.
     */
    public static boolean hasRole(String role) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(role) || authority.equals("ROLE_" + role));
    }
}
