package com.autumnus.spring_boot_starter_template.modules.users.service;

import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import com.autumnus.spring_boot_starter_template.modules.users.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final User user = userRepository.findByEmail(username)
                .or(() -> userRepository.findByUsername(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        final Set<SimpleGrantedAuthority> authorities = user.getRoleAssignments().stream()
                .map(assignment -> new SimpleGrantedAuthority("ROLE_" + assignment.getRole().getName().name()))
                .collect(Collectors.toUnmodifiableSet());
        return new UserPrincipal(user.getId(), user.getUuid(), user.getPasswordHash(), authorities, user.isActive());
    }
}
