package com.autumnus.spring_boot_starter_template.common.security;

import com.autumnus.spring_boot_starter_template.common.config.KeycloakProperties;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "application.security.keycloak", name = "enabled", havingValue = "true")
public class KeycloakService {

    private final Keycloak keycloak;
    private final KeycloakProperties keycloakProperties;

    public KeycloakService(Keycloak keycloak, KeycloakProperties keycloakProperties) {
        this.keycloak = keycloak;
        this.keycloakProperties = keycloakProperties;
    }

    private RealmResource getRealmResource() {
        return keycloak.realm(keycloakProperties.getRealm());
    }

    private UsersResource getUsersResource() {
        return getRealmResource().users();
    }

    /**
     * Create a new user in Keycloak
     */
    public String createUser(String username, String email, String password, String firstName, String lastName) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(false);

        Response response = getUsersResource().create(user);

        if (response.getStatus() != 201) {
            log.error("Failed to create user in Keycloak. Status: {}, Response: {}",
                    response.getStatus(), response.getStatusInfo());
            throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatusInfo());
        }

        String userId = extractUserIdFromResponse(response);
        log.info("User created in Keycloak with ID: {}", userId);

        // Set password
        if (password != null && !password.isEmpty()) {
            setUserPassword(userId, password, false);
        }

        response.close();
        return userId;
    }

    /**
     * Update user in Keycloak
     */
    public void updateUser(String keycloakId, String email, String username, String firstName, String lastName) {
        UserResource userResource = getUsersResource().get(keycloakId);
        UserRepresentation user = userResource.toRepresentation();

        if (email != null) user.setEmail(email);
        if (username != null) user.setUsername(username);
        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);

        userResource.update(user);
        log.info("User updated in Keycloak: {}", keycloakId);
    }

    /**
     * Delete user from Keycloak
     */
    public void deleteUser(String keycloakId) {
        Response response = getUsersResource().delete(keycloakId);
        if (response.getStatus() != 204) {
            log.error("Failed to delete user from Keycloak. Status: {}", response.getStatus());
            throw new RuntimeException("Failed to delete user from Keycloak");
        }
        response.close();
        log.info("User deleted from Keycloak: {}", keycloakId);
    }

    /**
     * Set user password
     */
    public void setUserPassword(String keycloakId, String password, boolean temporary) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(temporary);

        UserResource userResource = getUsersResource().get(keycloakId);
        userResource.resetPassword(credential);
        log.info("Password set for user: {}", keycloakId);
    }

    /**
     * Assign roles to user
     */
    public void assignRolesToUser(String keycloakId, List<String> roleNames) {
        UserResource userResource = getUsersResource().get(keycloakId);
        RealmResource realmResource = getRealmResource();

        List<RoleRepresentation> roles = roleNames.stream()
                .map(roleName -> realmResource.roles().get(roleName).toRepresentation())
                .collect(Collectors.toList());

        userResource.roles().realmLevel().add(roles);
        log.info("Roles assigned to user {}: {}", keycloakId, roleNames);
    }

    /**
     * Remove roles from user
     */
    public void removeRolesFromUser(String keycloakId, List<String> roleNames) {
        UserResource userResource = getUsersResource().get(keycloakId);
        RealmResource realmResource = getRealmResource();

        List<RoleRepresentation> roles = roleNames.stream()
                .map(roleName -> realmResource.roles().get(roleName).toRepresentation())
                .collect(Collectors.toList());

        userResource.roles().realmLevel().remove(roles);
        log.info("Roles removed from user {}: {}", keycloakId, roleNames);
    }

    /**
     * Get user by Keycloak ID
     */
    public UserRepresentation getUserById(String keycloakId) {
        return getUsersResource().get(keycloakId).toRepresentation();
    }

    /**
     * Get user by email
     */
    public UserRepresentation getUserByEmail(String email) {
        List<UserRepresentation> users = getUsersResource().search(email, true);
        return users.isEmpty() ? null : users.get(0);
    }

    /**
     * Get user by username
     */
    public UserRepresentation getUserByUsername(String username) {
        List<UserRepresentation> users = getUsersResource().search(username, true);
        return users.isEmpty() ? null : users.get(0);
    }

    /**
     * Enable/disable user
     */
    public void setUserEnabled(String keycloakId, boolean enabled) {
        UserResource userResource = getUsersResource().get(keycloakId);
        UserRepresentation user = userResource.toRepresentation();
        user.setEnabled(enabled);
        userResource.update(user);
        log.info("User {} enabled status set to: {}", keycloakId, enabled);
    }

    /**
     * Verify user email
     */
    public void setEmailVerified(String keycloakId, boolean verified) {
        UserResource userResource = getUsersResource().get(keycloakId);
        UserRepresentation user = userResource.toRepresentation();
        user.setEmailVerified(verified);
        userResource.update(user);
        log.info("User {} email verified status set to: {}", keycloakId, verified);
    }

    /**
     * Send verification email
     */
    public void sendVerificationEmail(String keycloakId) {
        UserResource userResource = getUsersResource().get(keycloakId);
        userResource.sendVerifyEmail();
        log.info("Verification email sent to user: {}", keycloakId);
    }

    /**
     * Extract user ID from Keycloak create response
     */
    private String extractUserIdFromResponse(Response response) {
        String location = response.getHeaderString("Location");
        if (location == null) {
            throw new RuntimeException("Unable to extract user ID from Keycloak response");
        }
        return location.substring(location.lastIndexOf('/') + 1);
    }

    /**
     * Get user roles
     */
    public List<String> getUserRoles(String keycloakId) {
        UserResource userResource = getUsersResource().get(keycloakId);
        return userResource.roles().realmLevel().listAll().stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toList());
    }
}
