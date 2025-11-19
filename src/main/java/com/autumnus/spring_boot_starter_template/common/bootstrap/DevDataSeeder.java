package com.autumnus.spring_boot_starter_template.common.bootstrap;

import com.autumnus.spring_boot_starter_template.modules.users.dto.UserCreateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.repository.UserRepository;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class DevDataSeeder {

    @Bean
    CommandLineRunner initDatabase(
            UserRepository userRepository,
            UserService userService
    ) {
        return args -> {
            if (userRepository.count() == 0) {
                userService.createUser(UserCreateRequest.builder()
                        .email("admin@example.com")
                        .username("admin")
                        .password("Admin123!")
                        .active(true)
                        .build());

                userService.createUser(UserCreateRequest.builder()
                        .email("user@example.com")
                        .username("user")
                        .password("User123!")
                        .active(true)
                        .build());
            }
        };
    }
}
