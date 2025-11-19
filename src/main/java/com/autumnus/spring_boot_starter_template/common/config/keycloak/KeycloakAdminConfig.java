package com.autumnus.spring_boot_starter_template.common.config.keycloak;

import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KeycloakAdminConfig {

    private final KeycloakAdminProperties keycloakAdminProperties;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakAdminProperties.getServerUrl())
                .realm("master") // We need to use the master realm to get a token for the admin user
                .grantType("password")
                .clientId("admin-cli") // Default client ID for admin operations
                .username(keycloakAdminProperties.getAdminUser())
                .password(keycloakAdminProperties.getAdminPassword())
                .resteasyClient(((ResteasyClientBuilder) ResteasyClientBuilder.newBuilder()).connectionPoolSize(10).build())
                .build();
    }
}
