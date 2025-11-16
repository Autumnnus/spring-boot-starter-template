package com.autumnus.spring_boot_starter_template.common.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts Keycloak JWT roles to Spring Security GrantedAuthorities
 */
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Extract realm roles
        Collection<String> realmRoles = extractRealmRoles(jwt);
        if (realmRoles != null) {
            authorities.addAll(realmRoles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList()));
        }

        // Extract resource roles (client-specific roles)
        Collection<String> resourceRoles = extractResourceRoles(jwt);
        if (resourceRoles != null) {
            authorities.addAll(resourceRoles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList()));
        }

        return authorities;
    }

    @SuppressWarnings("unchecked")
    private Collection<String> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            return (Collection<String>) realmAccess.get("roles");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Collection<String> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            // Try to get roles from the specific client
            for (Object value : resourceAccess.values()) {
                if (value instanceof Map) {
                    Map<String, Object> clientAccess = (Map<String, Object>) value;
                    if (clientAccess.containsKey("roles")) {
                        return (Collection<String>) clientAccess.get("roles");
                    }
                }
            }
        }
        return null;
    }
}
