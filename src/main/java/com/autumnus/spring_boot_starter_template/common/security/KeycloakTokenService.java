package com.autumnus.spring_boot_starter_template.common.security;

import com.autumnus.spring_boot_starter_template.common.config.KeycloakProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "application.security.keycloak", name = "enabled", havingValue = "true")
public class KeycloakTokenService {

    private final KeycloakProperties keycloakProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public KeycloakTokenService(KeycloakProperties keycloakProperties, ObjectMapper objectMapper) {
        this.keycloakProperties = keycloakProperties;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    /**
     * Obtain token from Keycloak using Direct Grant (Resource Owner Password Credentials)
     */
    public Map<String, Object> obtainToken(String username, String password) {
        String tokenUrl = keycloakProperties.getAuthServerUrl() + "/realms/" +
                keycloakProperties.getRealm() + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", keycloakProperties.getResource());
        body.add("client_secret", keycloakProperties.getCredentials().getSecret());
        body.add("username", username);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                Map<String, Object> tokenResponse = new HashMap<>();
                tokenResponse.put("access_token", jsonNode.get("access_token").asText());
                tokenResponse.put("refresh_token", jsonNode.get("refresh_token").asText());
                tokenResponse.put("expires_in", jsonNode.get("expires_in").asInt());
                tokenResponse.put("refresh_expires_in", jsonNode.get("refresh_expires_in").asInt());
                tokenResponse.put("token_type", jsonNode.get("token_type").asText());

                log.info("Token obtained successfully for user: {}", username);
                return tokenResponse;
            } else {
                throw new RuntimeException("Failed to obtain token from Keycloak");
            }
        } catch (Exception e) {
            log.error("Error obtaining token from Keycloak", e);
            throw new RuntimeException("Failed to obtain token from Keycloak: " + e.getMessage(), e);
        }
    }

    /**
     * Refresh token from Keycloak
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        String tokenUrl = keycloakProperties.getAuthServerUrl() + "/realms/" +
                keycloakProperties.getRealm() + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", keycloakProperties.getResource());
        body.add("client_secret", keycloakProperties.getCredentials().getSecret());
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                Map<String, Object> tokenResponse = new HashMap<>();
                tokenResponse.put("access_token", jsonNode.get("access_token").asText());
                tokenResponse.put("refresh_token", jsonNode.get("refresh_token").asText());
                tokenResponse.put("expires_in", jsonNode.get("expires_in").asInt());
                tokenResponse.put("refresh_expires_in", jsonNode.get("refresh_expires_in").asInt());
                tokenResponse.put("token_type", jsonNode.get("token_type").asText());

                log.info("Token refreshed successfully");
                return tokenResponse;
            } else {
                throw new RuntimeException("Failed to refresh token from Keycloak");
            }
        } catch (Exception e) {
            log.error("Error refreshing token from Keycloak", e);
            throw new RuntimeException("Failed to refresh token from Keycloak: " + e.getMessage(), e);
        }
    }

    /**
     * Logout from Keycloak (revoke token)
     */
    public void logout(String refreshToken) {
        String logoutUrl = keycloakProperties.getAuthServerUrl() + "/realms/" +
                keycloakProperties.getRealm() + "/protocol/openid-connect/logout";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", keycloakProperties.getResource());
        body.add("client_secret", keycloakProperties.getCredentials().getSecret());
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    logoutUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.NO_CONTENT || response.getStatusCode() == HttpStatus.OK) {
                log.info("User logged out successfully from Keycloak");
            } else {
                log.warn("Logout from Keycloak returned status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error logging out from Keycloak", e);
            throw new RuntimeException("Failed to logout from Keycloak: " + e.getMessage(), e);
        }
    }

    /**
     * Introspect token (validate token)
     */
    public boolean validateToken(String token) {
        String introspectUrl = keycloakProperties.getAuthServerUrl() + "/realms/" +
                keycloakProperties.getRealm() + "/protocol/openid-connect/token/introspect";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", keycloakProperties.getResource());
        body.add("client_secret", keycloakProperties.getCredentials().getSecret());
        body.add("token", token);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    introspectUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return jsonNode.get("active").asBoolean();
            }
            return false;
        } catch (Exception e) {
            log.error("Error validating token with Keycloak", e);
            return false;
        }
    }
}
