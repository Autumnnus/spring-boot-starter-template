package com.autumnus.spring_boot_starter_template.modules.users.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String email;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean active;

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
}
