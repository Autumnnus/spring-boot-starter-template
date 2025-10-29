package com.autumnus.spring_boot_starter_template.modules.auth.service;

import com.autumnus.spring_boot_starter_template.common.config.SecurityProperties;
import com.autumnus.spring_boot_starter_template.common.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceImpl implements JwtService {

    private final Key signingKey;
    private final SecurityProperties securityProperties;

    public JwtServiceImpl(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        final String secret = Objects.requireNonNull(securityProperties.getJwtSecret(), "JWT secret must be configured");
        if (secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret length must be at least 32 characters");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        final Instant now = Instant.now();
        final Long userId = userDetails instanceof UserPrincipal principal
                ? principal.getUserId()
                : null;
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(securityProperties.getAccessTokenTtl())))
                .claim("roles", userDetails.getAuthorities().stream().map(Object::toString).toList())
                .claim("uid", userId)
                .signWith(signingKey)
                .compact();
    }

    @Override
    public String generateRefreshToken(Long userId) {
        final Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(securityProperties.getRefreshTokenTtl())))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public Long extractUserId(String token) {
        return Long.parseLong(extractClaims(token).getSubject());
    }

    @Override
    public Claims extractClaims(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }
}
