package com.autumnus.spring_boot_starter_template.common.security;

import com.autumnus.spring_boot_starter_template.modules.users.entity.Role;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final UUID userUuid;
    private final String password;
    private final String email;
    private final Set<Role> roles;
    private final boolean active;
    private final Instant lockedUntil;

    public UserPrincipal(User user) {
        this.userId = user.getId();
        this.userUuid = user.getUuid();
        this.password = user.getPasswordHash();
        this.email = user.getEmail();
        this.roles = user.getRoles();
        this.active = user.isActive();
        this.lockedUntil = user.getLockedUntil();
    }

    public Long getUserId() {
        return userId;
    }

    public UUID getUserUuid() {
        return userUuid;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(Role::getName)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userUuid.toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return lockedUntil == null || lockedUntil.isBefore(Instant.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
