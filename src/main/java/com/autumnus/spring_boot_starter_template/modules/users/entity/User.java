package com.autumnus.spring_boot_starter_template.modules.users.entity;

import com.autumnus.spring_boot_starter_template.common.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uc_users_keycloak_id", columnNames = "keycloakId")
        }
)
@Getter
@Setter
public class User extends BaseEntity {

    @Column(nullable = false)
    private String keycloakId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String username;

    @Lob
    @Column(name = "profile_photo_manifest")
    private String profilePhotoManifest;

    private boolean active = true;
    private boolean emailVerified = false;
    private Instant lastLoginAt;
    private Instant passwordChangedAt;
    private Integer failedLoginAttempts = 0;
    private Instant lockedUntil;
}
