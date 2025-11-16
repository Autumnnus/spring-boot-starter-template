package com.autumnus.spring_boot_starter_template.common.security;

import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import com.autumnus.spring_boot_starter_template.modules.users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Service for synchronizing Keycloak users with local database.
 * This service maintains application-specific user data while authentication is handled by Keycloak.
 */
@Service
@Slf4j
public class KeycloakUserService {

    private final UserRepository userRepository;

    public KeycloakUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get or create user from Keycloak JWT token
     */
    @Transactional
    public User getOrCreateUserFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String keycloakUserId = jwt.getSubject();
            String email = jwt.getClaim("email");
            String username = jwt.getClaim("preferred_username");

            return getOrCreateUser(keycloakUserId, email, username);
        }

        throw new UnauthorizedException("No valid authentication found");
    }

    /**
     * Get or create user by Keycloak user ID
     */
    @Transactional
    public User getOrCreateUser(String keycloakUserId, String email, String username) {
        Optional<User> existingUser = userRepository.findByKeycloakUserId(keycloakUserId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update user info if changed
            if (!user.getEmail().equals(email) || !user.getUsername().equals(username)) {
                user.setEmail(email);
                user.setUsername(username);
                userRepository.save(user);
            }
            return user;
        }

        // Create new user
        User newUser = new User();
        newUser.setKeycloakUserId(keycloakUserId);
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setActive(true);
        newUser.setLastLoginAt(Instant.now());

        log.info("Creating new user from Keycloak: keycloakId={}, email={}", keycloakUserId, email);
        return userRepository.save(newUser);
    }

    /**
     * Get current user from security context
     */
    public Optional<User> getCurrentUser() {
        try {
            return Optional.of(getOrCreateUserFromToken());
        } catch (Exception e) {
            log.debug("Failed to get current user: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get current user ID from security context
     */
    public Long getCurrentUserId() {
        return getCurrentUser()
                .map(User::getId)
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found"));
    }

    /**
     * Update last login time for user
     */
    @Transactional
    public void updateLastLogin(String keycloakUserId) {
        userRepository.findByKeycloakUserId(keycloakUserId)
                .ifPresent(user -> {
                    user.setLastLoginAt(Instant.now());
                    userRepository.save(user);
                });
    }
}
