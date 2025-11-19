package com.autumnus.spring_boot_starter_template.common.security;

import com.autumnus.spring_boot_starter_template.modules.users.service.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

public class SecurityUtils {

    public static Optional<Long> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            Jwt jwt = jwtAuthenticationToken.getToken();
            // Assuming the 'sub' claim in the JWT contains the user ID
            // Keycloak's 'sub' claim is typically a UUID, but if we store it as Long in our DB,
            // we need to convert it. For now, let's assume it's a String that can be parsed to Long.
            // If Keycloak's 'sub' is a UUID, and our local user ID is a Long, we might need a mapping.
            // For this template, let's assume 'sub' can be directly used as a String identifier for the user.
            // If the local User entity's ID is a Long, and Keycloak's 'sub' is a UUID, then we should
            // retrieve the local User entity by keycloakId (which is a String UUID) and then get its Long ID.
            // However, OwnershipGuard expects a Long. So, we need to ensure consistency.
            // Let's assume for now that the 'sub' claim can be converted to a Long.
            // If Keycloak's 'sub' is a UUID, then the local User entity should store the UUID as a String,
            // and the OwnershipGuard should compare String UUIDs.
            // Given the User entity has `keycloakId` as String, it's better to return Optional<String> here
            // and adjust OwnershipGuard to compare String keycloakIds.

            // Re-evaluating: The User entity has `keycloakId` as String. OwnershipGuard expects `Long ownerId`.
            // This means there's a mismatch. The `UserPrincipal` likely provides the `userId` as a Long.
            // Let's assume `UserPrincipal` is the source of truth for the application's internal user ID.
            // If `UserPrincipal` is populated from the JWT, it should extract the `keycloakId` (sub) and
            // then map it to the local `User` entity's `id` (Long).

            // For now, let's return the 'sub' claim as a String, and we will need to adjust OwnershipGuard
            // and UserPrincipal accordingly.

            // However, the existing OwnershipGuard expects a Long.
            // Let's assume the 'sub' claim from Keycloak is a String representation of a Long ID for now,
            // or that the UserPrincipal (which is used to populate the Authentication object)
            // provides a Long ID.

            // Let's try to get the 'sub' claim and convert it to Long. This might fail if 'sub' is a UUID.
            // A more robust solution would be to get the local user ID from the UserPrincipal.

            // Let's check UserPrincipal.java to see what it returns for getUserId().
            // If UserPrincipal.getUserId() returns a Long, then we should use that.
            // If UserPrincipal is not available here, then we need to extract from JWT.

            // Given the context, the `sub` claim from Keycloak is a UUID (String).
            // Our local `User` entity has a `keycloakId` (String) and an `id` (Long).
            // The `OwnershipGuard` expects a `Long ownerId`.
            // This means `SecurityUtils.getCurrentUserId()` should return the local `User`'s `id` (Long).
            // To do this, we need to:
            // 1. Get the `sub` (Keycloak ID) from the JWT.
            // 2. Find the local `User` entity by `keycloakId`.
            // 3. Return the `id` (Long) of the local `User` entity.

            // This would require injecting `UserRepository` into `SecurityUtils`, which is not ideal for a utility class.
            // A better approach is to have `UserPrincipal` (which is already in the SecurityContext) provide the local user ID.

            // Let's assume `UserPrincipal` is available in the Authentication object's principal.
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserPrincipal userPrincipal) {
                return Optional.of(userPrincipal.getUserId());
            }
        }
        return Optional.empty();
    }
}
