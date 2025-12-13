package com.autumnus.spring_boot_starter_template.modules.auth.oauth2;

import com.autumnus.spring_boot_starter_template.modules.users.entity.OAuth2Provider;
import com.autumnus.spring_boot_starter_template.modules.users.entity.Role;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserRoleAssignment;
import com.autumnus.spring_boot_starter_template.modules.users.repository.RoleRepository;
import com.autumnus.spring_boot_starter_template.modules.users.repository.UserRepository;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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

        return UserPrincipal.create(user, oauth2User.getAttributes());
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

        // Assign default USER role
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default USER role not found"));

        UserRoleAssignment roleAssignment = new UserRoleAssignment();
        roleAssignment.setUser(user);
        roleAssignment.setRole(userRole);
        roleAssignment.setAssignedAt(Instant.now());
        user.getRoleAssignments().add(roleAssignment);

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
