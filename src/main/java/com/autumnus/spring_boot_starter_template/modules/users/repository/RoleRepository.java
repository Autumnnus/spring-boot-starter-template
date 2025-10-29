package com.autumnus.spring_boot_starter_template.modules.users.repository;

import com.autumnus.spring_boot_starter_template.modules.users.entity.Role;
import com.autumnus.spring_boot_starter_template.modules.users.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
