package com.autumnus.spring_boot_starter_template.modules.users.entity;

import com.autumnus.spring_boot_starter_template.common.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "email_verification_tokens",
        indexes = {
                @Index(name = "idx_email_verification_token", columnList = "token", unique = true),
                @Index(name = "idx_email_verification_expires", columnList = "expires_at")
        }
)
@Getter
@Setter
public class EmailVerificationToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_used", nullable = false)
    private boolean used = false;

    @Column(name = "used_at")
    private Instant usedAt;
}
