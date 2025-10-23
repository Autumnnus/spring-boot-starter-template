package com.autumnus.spring_boot_starter_template.common.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.security")
public class SecurityProperties {

    private String jwtSecret;
    private Duration accessTokenTtl = Duration.ofMinutes(15);
    private List<String> publicEndpoints = List.of(
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
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

    public List<String> getPublicEndpoints() {
        return publicEndpoints;
    }

    public void setPublicEndpoints(List<String> publicEndpoints) {
        this.publicEndpoints = publicEndpoints;
    }
}
