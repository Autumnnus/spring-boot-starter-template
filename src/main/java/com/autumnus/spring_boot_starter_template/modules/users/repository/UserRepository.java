package com.autumnus.spring_boot_starter_template.modules.users.repository;

import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUuid(UUID uuid);

    Optional<User> findByUsername(String username);
}
