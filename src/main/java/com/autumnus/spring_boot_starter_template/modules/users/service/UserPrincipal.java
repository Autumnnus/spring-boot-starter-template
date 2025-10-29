package com.autumnus.spring_boot_starter_template.modules.users.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final UUID uuid;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean active;

    public UserPrincipal(
            Long userId,
            UUID uuid,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            boolean active
    ) {
        this.userId = userId;
        this.uuid = uuid;
        this.password = password;
        this.authorities = authorities == null ? Collections.emptyList() : authorities;
        this.active = active;
    }

    public static UserPrincipal fromToken(Long userId, String uuid, Collection<? extends GrantedAuthority> authorities) {
        return new UserPrincipal(userId, UUID.fromString(uuid), "", authorities, true);
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
        return uuid.toString();
    }

    public Long getUserId() {
        return userId;
    }

    public UUID getUuid() {
        return uuid;
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
}
