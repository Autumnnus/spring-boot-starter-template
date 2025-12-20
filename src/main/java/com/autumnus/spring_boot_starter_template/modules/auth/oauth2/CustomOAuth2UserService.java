package com.autumnus.spring_boot_starter_template.modules.auth.oauth2;

import com.autumnus.spring_boot_starter_template.modules.users.entity.*;
import com.autumnus.spring_boot_starter_template.modules.users.repository.RoleRepository;
import com.autumnus.spring_boot_starter_template.modules.users.repository.UserRepository;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Provider provider = OAuth2Provider.valueOf(registrationId.toUpperCase());

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String pictureUrl = oauth2User.getAttribute("picture");
        String providerId = oauth2User.getAttribute("sub");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(email, name, provider, providerId, pictureUrl));

        // Update OAuth2 info if user exists
        if (user.getOauth2Provider() == null) {
            user.setOauth2Provider(provider);
            user.setOauth2ProviderId(providerId);
            user.setOauth2ProfilePictureUrl(pictureUrl);
            user.setEmailVerified(true); // OAuth2 users are email verified
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        log.info("OAuth2 login successful for user: {} with provider: {}", email, provider);

        Set<GrantedAuthority> authorities = new HashSet<>();
        for (RoleName roleName : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName.name()));
            roleRepository.findByName(roleName).ifPresent(role -> {
                role.getPermissions().forEach(permission -> 
                    authorities.add(new SimpleGrantedAuthority(permission.getResource() + ":" + permission.getAction()))
                );
            });
        }

        return UserPrincipal.create(user, oauth2User.getAttributes(), authorities);
    }

    private User createNewUser(String email, String name, OAuth2Provider provider, String providerId, String pictureUrl) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(generateUsername(email));
        user.setOauth2Provider(provider);
        user.setOauth2ProviderId(providerId);
        user.setOauth2ProfilePictureUrl(pictureUrl);
        user.setEmailVerified(true); // OAuth2 users are email verified
        user.setActive(true);

        // Verify default USER role exists
        if (roleRepository.findByName(RoleName.USER).isEmpty()) {
            throw new RuntimeException("Default USER role not found");
        }
        user.getRoles().add(RoleName.USER);

        log.info("Creating new OAuth2 user: {} with provider: {}", email, provider);

        return userRepository.save(user);
    }

    private String generateUsername(String email) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int suffix = 1;

        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + suffix++;
        }

        return username;
    }
}