package com.autumnus.spring_boot_starter_template.modules.auth.repository;

import com.autumnus.spring_boot_starter_template.modules.auth.entity.PasswordResetToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    @Query("delete from PasswordResetToken t where t.expiresAt < ?1 or t.used = true")
    int deleteExpiredOrUsed(Instant now);
}
