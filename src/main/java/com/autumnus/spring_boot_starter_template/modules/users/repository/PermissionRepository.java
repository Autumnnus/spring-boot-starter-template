package com.autumnus.spring_boot_starter_template.modules.users.repository;

import com.autumnus.spring_boot_starter_template.modules.users.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);
}
