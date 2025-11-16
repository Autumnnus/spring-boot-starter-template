package com.autumnus.spring_boot_starter_template.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application.security.keycloak")
@Getter
@Setter
public class KeycloakProperties {

    private boolean enabled;
    private String authServerUrl;
    private String realm;
    private String resource;
    private Credentials credentials = new Credentials();
    private Admin admin = new Admin();

    @Getter
    @Setter
    public static class Credentials {
        private String secret;
    }

    @Getter
    @Setter
    public static class Admin {
        private String username;
        private String password;
    }
}
