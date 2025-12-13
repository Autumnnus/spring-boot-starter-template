package com.autumnus.spring_boot_starter_template.modules.users.service;

import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails, OAuth2User {

    private final Long userId;
    private final String email;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean active;
    private Map<String, Object> attributes;

    public UserPrincipal(
            Long userId,
            String email,
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            boolean active
    ) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.password = password;
        this.authorities = authorities == null ? Collections.emptyList() : authorities;
        this.active = active;
    }

    public static UserPrincipal fromToken(
            Long userId,
            String email,
            String username,
            Collection<? extends GrantedAuthority> authorities
    ) {
        return new UserPrincipal(userId, email, username, "", authorities, true);
    }

    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        Collection<GrantedAuthority> authorities = user.getRoleAssignments().stream()
                .flatMap(assignment -> {
                    var role = assignment.getRole();
                    var roleAuthorities = role.getPermissions().stream()
                            .map(permission -> new SimpleGrantedAuthority(permission.getResource() + ":" + permission.getAction()))
                            .collect(Collectors.toList());
                    roleAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                    return roleAuthorities.stream();
                })
                .collect(Collectors.toList());

        UserPrincipal userPrincipal = new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getPasswordHash() != null ? user.getPasswordHash() : "",
                authorities,
                user.isActive()
        );
        userPrincipal.attributes = attributes;
        return userPrincipal;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email != null ? email : String.valueOf(userId);
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getAccountUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    // OAuth2User methods
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
