package com.autumnus.spring_boot_starter_template.common.bootstrap;

import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserRole;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserStatus;
import com.autumnus.spring_boot_starter_template.modules.users.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class DevDataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {

                User admin = new User();
                admin.setEmail("admin@example.com");
                admin.setDisplayName("Admin User");
                admin.setStatus(UserStatus.ACTIVE);
                admin.setRoles(Set.of(UserRole.ADMIN, UserRole.USER));

                userRepository.save(admin);

                User standardUser = new User();
                standardUser.setEmail("user@example.com");
                standardUser.setDisplayName("Standard User");
                standardUser.setStatus(UserStatus.ACTIVE);
                standardUser.setRoles(Set.of(UserRole.USER));

                userRepository.save(standardUser);

                User inactiveUser = new User();
                inactiveUser.setEmail("inactive@example.com");
                inactiveUser.setDisplayName("Inactive User");
                inactiveUser.setStatus(UserStatus.INACTIVE);
                inactiveUser.setRoles(Set.of(UserRole.USER));

                userRepository.save(inactiveUser);

                System.out.println("Mock kullanıcı verileri veritabanına eklendi.");
            }
        };
    }
}
