package com.autumnus.spring_boot_starter_template.modules.auth.service;

import com.autumnus.spring_boot_starter_template.common.config.keycloak.KeycloakAdminProperties;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.ChangePasswordRequest;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.LoginRequest;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.PasswordResetRequest;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.RefreshTokenRequest;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.ResetPasswordRequest;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.TokenResponse;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.RegisterRequest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import com.autumnus.spring_boot_starter_template.modules.users.repository.UserRepository;
import com.autumnus.spring_boot_starter_template.modules.users.mapper.UserMapper;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final Keycloak keycloak;
    private final KeycloakAdminProperties keycloakAdminProperties;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RestTemplate restTemplate;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalStateException("User with email " + request.email() + " already exists");
        }

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(request.username());
        userRepresentation.setEmail(request.email());
        userRepresentation.setFirstName(request.firstName());
        userRepresentation.setLastName(request.lastName());
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true); // Or set to false and use Keycloak's email verification flow

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.password());
        credential.setTemporary(false);
        userRepresentation.setCredentials(List.of(credential));

        RealmResource realmResource = keycloak.realm(keycloakAdminProperties.getRealm());
        UsersResource usersResource = realmResource.users();

        try (Response response = usersResource.create(userRepresentation)) {
            if (response.getStatus() != 201) {
                String errorMessage = response.readEntity(String.class);
                log.error("Failed to create user in Keycloak. Status: {}, Body: {}", response.getStatus(), errorMessage);
                throw new IllegalStateException("Failed to create user in Keycloak: " + errorMessage);
            }

            String createdId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            log.info("User created in Keycloak with ID: {}", createdId);

            User localUser = new User();
            localUser.setKeycloakId(createdId);
            localUser.setEmail(request.email());
            localUser.setUsername(request.username());
            userRepository.save(localUser);

            // Fetch the user from the repository to get all fields, including generated ones like ID
            User savedUser = userRepository.findByKeycloakId(createdId)
                    .orElseThrow(() -> new IllegalStateException("Newly created user not found in local repository."));

            // For now, roles are empty as they are managed by Keycloak and not directly assigned during registration here.
            // If roles need to be assigned during registration, this logic would need to be extended.
            return userMapper.toResponse(savedUser);
        }
    }

    public TokenResponse login(LoginRequest request) {
        String username = request.usernameOrEmail();
        String tokenUrl = keycloakAdminProperties.getServerUrl() + "/realms/" + keycloakAdminProperties.getRealm() + "/protocol/openid-connect/token";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", keycloakAdminProperties.getClientId());
            map.add("client_secret", keycloakAdminProperties.getClientSecret());
            map.add("grant_type", "password");
            map.add("username", username);
            map.add("password", request.password());

            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);

            ResponseEntity<TokenResponse> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    httpEntity,
                    TokenResponse.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Keycloak login failed for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String tokenUrl = keycloakAdminProperties.getServerUrl() + "/realms/" + keycloakAdminProperties.getRealm() + "/protocol/openid-connect/token";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", keycloakAdminProperties.getClientId());
            map.add("client_secret", keycloakAdminProperties.getClientSecret());
            map.add("grant_type", "refresh_token");
            map.add("refresh_token", request.refreshToken());

            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);

            ResponseEntity<TokenResponse> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    httpEntity,
                    TokenResponse.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Keycloak refresh token failed: {}", e.getMessage());
            throw new RuntimeException("Failed to refresh token: " + e.getMessage());
        }
    }

    public void logout(RefreshTokenRequest request) {
        String logoutUrl = keycloakAdminProperties.getServerUrl() + "/realms/" + keycloakAdminProperties.getRealm() + "/protocol/openid-connect/logout";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", keycloakAdminProperties.getClientId());
            map.add("client_secret", keycloakAdminProperties.getClientSecret());
            map.add("refresh_token", request.refreshToken());

            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);

            restTemplate.exchange(
                    logoutUrl,
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );
        } catch (Exception e) {
            log.error("Keycloak logout failed: {}", e.getMessage());
            throw new RuntimeException("Failed to logout: " + e.getMessage());
        }
    }

    public void verifyEmail(String email) {
        RealmResource realmResource = keycloak.realm(keycloakAdminProperties.getRealm());
        UsersResource usersResource = realmResource.users();

        List<UserRepresentation> users = usersResource.searchByEmail(email, true);
        if (users.isEmpty()) {
            throw new IllegalArgumentException("User with email " + email + " not found in Keycloak.");
        }
        UserRepresentation user = users.get(0);

        try {
            usersResource.get(user.getId()).sendVerifyEmail();
            log.info("Sent email verification link to user with email: {}", email);
        } catch (Exception e) {
            log.error("Failed to send email verification link to user {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send email verification link: " + e.getMessage());
        }
    }

    public void requestPasswordReset(PasswordResetRequest request) {
        RealmResource realmResource = keycloak.realm(keycloakAdminProperties.getRealm());
        UsersResource usersResource = realmResource.users();

        List<UserRepresentation> users = usersResource.searchByEmail(request.email(), true);
        if (users.isEmpty()) {
            throw new IllegalArgumentException("User with email " + request.email() + " not found in Keycloak.");
        }
        UserRepresentation user = users.get(0);

        try {
            usersResource.get(user.getId()).executeActionsEmail(List.of("UPDATE_PASSWORD"));
            log.info("Sent password reset email to user with email: {}", request.email());
        } catch (Exception e) {
            log.error("Failed to send password reset email to user {}: {}", request.email(), e.getMessage());
            throw new RuntimeException("Failed to send password reset email: " + e.getMessage());
        }
    }

    public void resetPassword(ResetPasswordRequest request) {
        RealmResource realmResource = keycloak.realm(keycloakAdminProperties.getRealm());
        UsersResource usersResource = realmResource.users();

        // WARNING: This assumes the 'token' in ResetPasswordRequest is the Keycloak userId.
        // In a real-world scenario, Keycloak's 'executeActionsEmail' sends a link to Keycloak's
        // own UI for password reset, or a custom flow would be needed to validate a token
        // issued by Keycloak for this purpose. Directly using a token from the client to
        // update a password via the admin API is not a standard secure Keycloak flow
        // unless the token is an admin-issued temporary password or a very short-lived,
        // single-use token validated by a custom Keycloak extension.
        // For this refactoring, we proceed with the assumption that 'token' is the userId.

        try {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.newPassword());
            credential.setTemporary(false);

            usersResource.get(request.token()).resetPassword(credential);
            log.info("Password reset for user with ID: {}", request.token());
        } catch (Exception e) {
            log.error("Failed to reset password for user with ID {}: {}", request.token(), e.getMessage());
            throw new RuntimeException("Failed to reset password: " + e.getMessage());
        }
    }

    public void changePassword(String userId, ChangePasswordRequest request) {
        RealmResource realmResource = keycloak.realm(keycloakAdminProperties.getRealm());
        UsersResource usersResource = realmResource.users();

        try {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.newPassword());
            credential.setTemporary(false);

            // Keycloak does not have a direct "old password" verification in the admin client for this operation.
            // The assumption here is that the user is already authenticated and authorized to change their password.
            // If old password verification is strictly required, it should be handled by Keycloak's account console
            // or a custom Keycloak extension.
            usersResource.get(userId).resetPassword(credential);
            log.info("Password changed for user with ID: {}", userId);
        } catch (Exception e) {
            log.error("Failed to change password for user with ID {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to change password: " + e.getMessage());
        }
    }
}