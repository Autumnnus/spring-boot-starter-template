package com.autumnus.spring_boot_starter_template.modules.users.repository;

import com.autumnus.spring_boot_starter_template.modules.users.entity.UserRoleAssignment;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserRoleAssignment.UserRoleAssignmentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, UserRoleAssignmentId> {

    List<UserRoleAssignment> findByUser_Id(Long userId);
}
