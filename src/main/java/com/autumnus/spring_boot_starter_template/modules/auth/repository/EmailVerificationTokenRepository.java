package com.autumnus.spring_boot_starter_template.modules.auth.repository;

import com.autumnus.spring_boot_starter_template.modules.auth.entity.EmailVerificationToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    @Modifying
    @Query("delete from EmailVerificationToken t where t.expiresAt < ?1 or t.used = true")
    int deleteExpiredOrUsed(Instant now);
}
