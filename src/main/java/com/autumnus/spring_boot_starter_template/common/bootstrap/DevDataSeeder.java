package com.autumnus.spring_boot_starter_template.common.bootstrap;

import com.autumnus.spring_boot_starter_template.modules.users.entity.Role;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserStatus;
import com.autumnus.spring_boot_starter_template.modules.users.repository.RoleRepository;
import com.autumnus.spring_boot_starter_template.modules.users.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Configuration
public class DevDataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (roleRepository.count() == 0) {
                final List<Role> roles = List.of(
                        createRole("ADMIN", "Administrator"),
                        createRole("USER", "Standard user"),
                        createRole("MODERATOR", "Content moderator")
                );
                roleRepository.saveAll(roles);
            }

            if (userRepository.count() == 0) {
                final Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
                final Role userRole = roleRepository.findByName("USER").orElseThrow();
                final Role moderatorRole = roleRepository.findByName("MODERATOR").orElseThrow();

                userRepository.save(createUser(
                        "admin@example.com",
                        "admin",
                        "Admin User",
                        passwordEncoder,
                        Set.of(adminRole, userRole)
                ));

                userRepository.save(createUser(
                        "user@example.com",
                        "standard",
                        "Standard User",
                        passwordEncoder,
                        Set.of(userRole)
                ));

                final User inactive = createUser(
                        "inactive@example.com",
                        "inactive",
                        "Inactive User",
                        passwordEncoder,
                        Set.of(userRole)
                );
                inactive.setStatus(UserStatus.INACTIVE);
                inactive.setActive(false);
                userRepository.save(inactive);

                final User moderator = createUser(
                        "moderator@example.com",
                        "moderator",
                        "Moderator User",
                        passwordEncoder,
                        Set.of(moderatorRole, userRole)
                );
                userRepository.save(moderator);

                System.out.println("Mock kullanıcı ve rol verileri veritabanına eklendi.");
            }
        };
    }

    private Role createRole(String name, String description) {
        final Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        return role;
    }

    private User createUser(
            String email,
            String username,
            String displayName,
            PasswordEncoder passwordEncoder,
            Set<Role> roles
    ) {
        final User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setStatus(UserStatus.ACTIVE);
        user.setActive(true);
        user.setEmailVerified(true);
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setPasswordChangedAt(Instant.now());
        user.setRoles(roles);
        return user;
    }
}
