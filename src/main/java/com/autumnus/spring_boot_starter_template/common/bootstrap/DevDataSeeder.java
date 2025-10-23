package com.autumnus.spring_boot_starter_template.common.bootstrap;

import com.autumnus.spring_boot_starter_template.modules.users.dto.UserCreateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserRole;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserStatus;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private final UserService userService;

    public DevDataSeeder(UserService userService) {
        this.userService = userService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // idempotent: varsa tekrar ekleme
        seedUser("admin@example.com", "Admin", "User", UserRole.ROLE_ADMIN);
        seedUser("mod@example.com", "Mod", "User", UserRole.ROLE_MODERATOR);
        seedUser("user@example.com", "Regular", "User", UserRole.ROLE_USER);
    }

    private void seedUser(String email, String name, String surname, UserRole role) {
        // userService tarafında "mail var mı?" kontrolü yapıyorsun (ideal)
        try {
            var req = UserCreateRequest.builder()
                    .email(email)
                    .password("Passw0rd!")
                    .name(name)
                    .surname(surname)
                    .role(role)
                    .status(UserStatus.ACTIVE)
                    .build();
            userService.createUser(req);
        } catch (Exception ignored) {
            // already exists vs. validation vb. durumlarda sessiz geç
        }
    }
}
