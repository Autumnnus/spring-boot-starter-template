package com.autumnus.spring_boot_starter_template.modules.auth.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {

    private final String accessToken;
    private final Instant accessTokenExpiresAt;
    private final String refreshToken;
    private final Instant refreshTokenExpiresAt;
}
