package com.autumnus.spring_boot_starter_template.modules.users.entity;

import com.autumnus.spring_boot_starter_template.common.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email", unique = true)
        }
)
@Getter
@Setter
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = true)
    private String passwordHash;

    @Column(nullable = false, unique = true)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth2_provider")
    private OAuth2Provider oauth2Provider;

    @Column(name = "oauth2_provider_id")
    private String oauth2ProviderId;

    @Column(name = "oauth2_profile_picture_url", length = 1024)
    private String oauth2ProfilePictureUrl;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "is_email_verified", nullable = false)
    private boolean emailVerified = false;

    private Instant lastLoginAt;

    private Instant passwordChangedAt;

    @Column(nullable = false)
    private Integer failedLoginAttempts = 0;

    private Instant lockedUntil;

    @Convert(converter = RoleSetConverter.class)
    @Column(name = "roles", nullable = false)
    private Set<RoleName> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RefreshToken> refreshTokens = new HashSet<>();

    @Lob
    @Column(name = "profile_photo_manifest")
    private String profilePhotoManifest;
}
