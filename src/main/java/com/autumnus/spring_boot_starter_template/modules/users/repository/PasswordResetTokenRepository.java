package com.autumnus.spring_boot_starter_template.modules.users.repository;

import com.autumnus.spring_boot_starter_template.modules.users.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByExpiresAtBefore(Instant expiresAt);
}
