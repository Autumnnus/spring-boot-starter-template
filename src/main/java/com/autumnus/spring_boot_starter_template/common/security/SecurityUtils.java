package com.autumnus.spring_boot_starter_template.common.security;

import com.autumnus.spring_boot_starter_template.modules.users.service.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Get current user ID from security context.
     * Supports both legacy UserPrincipal and Keycloak JWT authentication.
     */
    public static Optional<Long> getCurrentUserId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return Optional.empty();
        }

        final Object principal = authentication.getPrincipal();

        // Handle Keycloak JWT authentication
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
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
        }

        // Handle legacy UserPrincipal authentication
        if (principal instanceof UserPrincipal userPrincipal) {
            return Optional.ofNullable(userPrincipal.getUserId());
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
     * Get current user email from security context
     */
    public static Optional<String> getCurrentUserEmail() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return Optional.ofNullable(jwt.getClaim("email"));
        }
        return Optional.empty();
    }

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
