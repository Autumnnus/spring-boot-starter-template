package com.autumnus.spring_boot_starter_template.modules.auth.repository;

import com.autumnus.spring_boot_starter_template.modules.auth.entity.RefreshToken;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUser(User user);

    @Modifying
    @Query("update RefreshToken t set t.revoked = true, t.revokedAt = ?2 where t.user.id = ?1 and t.revoked = false")
    int revokeAllByUserId(Long userId, Instant revokedAt);

    @Modifying
    @Query("delete from RefreshToken t where t.expiresAt < ?1")
    int deleteAllExpiredBefore(Instant expiry);
}
