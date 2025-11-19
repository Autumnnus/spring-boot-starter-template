package com.autumnus.spring_boot_starter_template.common.security;

import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import com.autumnus.spring_boot_starter_template.modules.users.repository.UserRepository;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final KeycloakRealmRoleConverter keycloakRealmRoleConverter;
    private final UserRepository userRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = keycloakRealmRoleConverter.convert(jwt);

        String keycloakId = jwt.getSubject();
        Optional<User> userOptional = userRepository.findByKeycloakId(keycloakId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            UserPrincipal userPrincipal = UserPrincipal.fromToken(
                    user.getId(),
                    user.getEmail(),
                    user.getUsername(),
                    authorities
            );
            return new JwtAuthenticationToken(jwt, authorities, userPrincipal.getUsername());
        } else {
            // If the user is not found in our local database, we can still authenticate them
            // based on the JWT, but without our local user details.
            // In a typical scenario, users should be provisioned in our local DB upon first login/registration.
            // For now, we'll create a UserPrincipal with minimal info if not found locally.
            // This might indicate a misconfiguration or a user that was deleted locally but still exists in Keycloak.
            UserPrincipal userPrincipal = UserPrincipal.fromToken(
                    null, // No local user ID
                    jwt.getClaimAsString("email"),
                    jwt.getClaimAsString("preferred_username"),
                    authorities
            );
            return new JwtAuthenticationToken(jwt, authorities, userPrincipal.getUsername());
        }
    }
}
