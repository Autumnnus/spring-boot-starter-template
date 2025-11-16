package com.autumnus.spring_boot_starter_template.modules.users.entity;

import com.autumnus.spring_boot_starter_template.common.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * User entity for storing application-specific user data.
 * Authentication and core user management is handled by Keycloak.
 * This entity stores only application-specific metadata.
 */
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_keycloak_id", columnList = "keycloak_user_id", unique = true),
                @Index(name = "idx_users_email", columnList = "email", unique = true)
        }
)
@Getter
@Setter
public class User extends BaseEntity {

    /**
     * Keycloak user ID - primary reference to the user in Keycloak
     */
    @Column(name = "keycloak_user_id", nullable = false, unique = true)
    private String keycloakUserId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    private Instant lastLoginAt;

    /**
     * Legacy password hash - kept for backward compatibility
     * Will be removed in future versions as authentication is handled by Keycloak
     */
    @Deprecated
    @Column(name = "password_hash")
    private String passwordHash;

    @Lob
    @Column(name = "profile_photo_manifest")
    private String profilePhotoManifest;
}
