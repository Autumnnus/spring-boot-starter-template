package com.autumnus.spring_boot_starter_template.modules.auth.service;

import com.autumnus.spring_boot_starter_template.common.config.SecurityProperties;
import com.autumnus.spring_boot_starter_template.common.security.JwtTokenProvider;
import com.autumnus.spring_boot_starter_template.modules.users.entity.RefreshToken;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import com.autumnus.spring_boot_starter_template.modules.users.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class TokenServiceImpl implements TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityProperties securityProperties;

    public TokenServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            JwtTokenProvider jwtTokenProvider,
            SecurityProperties securityProperties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.securityProperties = securityProperties;
    }

    @Override
    public RefreshToken createRefreshToken(User user, String deviceInfo, String ipAddress) {
        final RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(jwtTokenProvider.generateRefreshToken(user));
        token.setExpiresAt(Instant.now().plus(securityProperties.getRefreshTokenTtl()));
        token.setDeviceInfo(deviceInfo);
        token.setIpAddress(ipAddress);
        return refreshTokenRepository.save(token);
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        final RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenValidationException("REFRESH_TOKEN_NOT_FOUND", "Refresh token not found"));
        if (refreshToken.isRevoked()) {
            throw new TokenValidationException("REFRESH_TOKEN_REVOKED", "Refresh token has been revoked");
        }
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenValidationException("REFRESH_TOKEN_EXPIRED", "Refresh token has expired");
        }
        if (!jwtTokenProvider.validateToken(token)) {
            throw new TokenValidationException("REFRESH_TOKEN_INVALID", "Refresh token is invalid");
        }
        return refreshToken;
    }

    @Override
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshToken.setRevokedAt(Instant.now());
            refreshTokenRepository.save(refreshToken);
        });
    }

    @Override
    public void revokeAllUserTokens(Long userId) {
        final List<RefreshToken> tokens = refreshTokenRepository.findByUserIdAndRevokedFalse(userId);
        final Instant now = Instant.now();
        tokens.forEach(token -> {
            token.setRevoked(true);
            token.setRevokedAt(now);
        });
        refreshTokenRepository.saveAll(tokens);
    }

    @Override
    public void cleanExpiredTokens() {
        final List<RefreshToken> expired = refreshTokenRepository.findByExpiresAtBefore(Instant.now());
        refreshTokenRepository.deleteAll(expired);
    }
}
