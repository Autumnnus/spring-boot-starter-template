package com.autumnus.spring_boot_starter_template.common.config.keycloak;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "keycloak.admin")
public class KeycloakAdminProperties {
    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    private String adminUser;
    private String adminPassword;
}
