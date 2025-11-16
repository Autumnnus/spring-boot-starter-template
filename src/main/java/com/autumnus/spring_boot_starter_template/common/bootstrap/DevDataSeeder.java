package com.autumnus.spring_boot_starter_template.common.bootstrap;

import com.autumnus.spring_boot_starter_template.modules.users.dto.UserCreateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.entity.Role;
import com.autumnus.spring_boot_starter_template.modules.users.entity.RoleName;
import com.autumnus.spring_boot_starter_template.modules.users.repository.RoleRepository;
import com.autumnus.spring_boot_starter_template.modules.users.repository.UserRepository;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.EnumSet;
import java.util.Set;

@Configuration
@Profile("!test")
public class DevDataSeeder {

    @Bean
    CommandLineRunner initDatabase(
            RoleRepository roleRepository,
            UserRepository userRepository,
            UserService userService
    ) {
        return args -> {
            if (roleRepository.count() == 0) {
                for (RoleName roleName : RoleName.values()) {
                    final Role role = new Role();
                    role.setName(roleName);
                    role.setDescription(roleName.name().toLowerCase() + " role");
                    roleRepository.save(role);
                }
            }

            if (userRepository.count() == 0) {
                userService.createUser(UserCreateRequest.builder()
                        .email("admin@example.com")
                        .username("admin")
                        .password("Admin123!")
                        .roles(EnumSet.of(RoleName.ADMIN, RoleName.USER))
                        .active(true)
                        .build());

                userService.createUser(UserCreateRequest.builder()
                        .email("user@example.com")
                        .username("user")
                        .password("User123!")
                        .roles(Set.of(RoleName.USER))
                        .active(true)
                        .build());
            }
        };
    }
}
