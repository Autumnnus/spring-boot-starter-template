package com.autumnus.spring_boot_starter_template.common.security;

import com.autumnus.spring_boot_starter_template.common.config.SecurityProperties;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final SecurityProperties properties;

    public JwtTokenProvider(SecurityProperties properties) {
        this.properties = properties;
        final String secret = Objects.requireNonNull(properties.getJwtSecret(), "JWT secret must be configured");
        if (secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret length must be at least 32 characters");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserDetails userDetails) {
        if (!(userDetails instanceof UserPrincipal principal)) {
            throw new IllegalArgumentException("UserDetails must be an instance of UserPrincipal");
        }
        return buildToken(principal.getUuid().toString(),
                principal.getUserId(),
                extractRoleNames(principal.getAuthorities()),
                properties.getAccessTokenTtl(),
                Map.of("type", "access"));
    }

    public String generateRefreshToken(com.autumnus.spring_boot_starter_template.modules.users.entity.User user) {
        return buildToken(user.getUuid().toString(),
                user.getId(),
                user.getRoleAssignments().stream()
                        .map(assignment -> "ROLE_" + assignment.getRole().getName().name())
                        .collect(Collectors.toSet()),
                properties.getRefreshTokenTtl(),
                Map.of("type", "refresh"));
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public Long extractUserId(String token) {
        final Claims claims = parseClaims(token);
        final Number userId = claims.get("uid", Number.class);
        return userId != null ? userId.longValue() : null;
    }

    public Claims extractClaims(String token) {
        return parseClaims(token);
    }

    public Authentication toAuthentication(String token) {
        final Claims claims = parseClaims(token);
        final String subject = claims.getSubject();
        final List<String> roles = claims.get("roles", List.class);
        final List<GrantedAuthority> authorities = roles == null
                ? List.of()
                : roles.stream()
                .map(SimpleGrantedAuthority::new)
                .map(a -> (GrantedAuthority) a)
                .toList();
        final UserPrincipal principal = UserPrincipal.fromToken(
                claims.get("uid", Number.class).longValue(),
                subject,
                authorities
        );
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String buildToken(
            String subject,
            Long userId,
            Collection<String> roles,
            java.time.Duration ttl,
            Map<String, Object> attributes
    ) {
        final Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .claim("uid", userId)
                .claim("roles", roles)
                .addClaims(attributes)
                .signWith(signingKey)
                .compact();
    }

    private Collection<String> extractRoleNames(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream().map(GrantedAuthority::getAuthority).toList();
    }
}
