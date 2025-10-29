package com.autumnus.spring_boot_starter_template.modules.auth.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record TokenResponse(
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        String tokenType
) {
}
