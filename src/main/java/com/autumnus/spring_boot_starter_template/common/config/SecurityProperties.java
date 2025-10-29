package com.autumnus.spring_boot_starter_template.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "application.security")
public class SecurityProperties {

    private String jwtSecret;
    private Duration accessTokenTtl = Duration.ofMinutes(15);
    private Duration refreshTokenTtl = Duration.ofDays(7);
    private Duration emailVerificationTokenTtl = Duration.ofHours(24);
    private Duration passwordResetTokenTtl = Duration.ofHours(1);
    private List<String> publicEndpoints = List.of(
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/v1/auth/**"
    );

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public void setAccessTokenTtl(Duration accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }

    public Duration getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    public void setRefreshTokenTtl(Duration refreshTokenTtl) {
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public Duration getEmailVerificationTokenTtl() {
        return emailVerificationTokenTtl;
    }

    public void setEmailVerificationTokenTtl(Duration emailVerificationTokenTtl) {
        this.emailVerificationTokenTtl = emailVerificationTokenTtl;
    }

    public Duration getPasswordResetTokenTtl() {
        return passwordResetTokenTtl;
    }

    public void setPasswordResetTokenTtl(Duration passwordResetTokenTtl) {
        this.passwordResetTokenTtl = passwordResetTokenTtl;
    }

    public List<String> getPublicEndpoints() {
        return publicEndpoints;
    }

    public void setPublicEndpoints(List<String> publicEndpoints) {
        this.publicEndpoints = publicEndpoints;
    }
}
