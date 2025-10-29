package com.autumnus.spring_boot_starter_template.modules.auth.service;

import com.autumnus.spring_boot_starter_template.common.config.SecurityProperties;
import com.autumnus.spring_boot_starter_template.modules.auth.entity.RefreshToken;
import com.autumnus.spring_boot_starter_template.modules.auth.repository.RefreshTokenRepository;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import java.time.Instant;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TokenServiceImpl implements TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityProperties securityProperties;

    public TokenServiceImpl(RefreshTokenRepository refreshTokenRepository, SecurityProperties securityProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.securityProperties = securityProperties;
    }

    @Override
    public RefreshToken createRefreshToken(User user, String deviceInfo, String ipAddress) {
        final RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plus(securityProperties.getRefreshTokenTtl()));
        refreshToken.setDeviceInfo(deviceInfo);
        refreshToken.setIpAddress(ipAddress);
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        final RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }
        return refreshToken;
    }

    @Override
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            rt.setRevokedAt(Instant.now());
            refreshTokenRepository.save(rt);
        });
    }

    @Override
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId, Instant.now());
    }

    @Override
    @Scheduled(cron = "0 0 * * * *")
    public void cleanExpiredTokens() {
        refreshTokenRepository.deleteAllExpiredBefore(Instant.now());
    }
}
