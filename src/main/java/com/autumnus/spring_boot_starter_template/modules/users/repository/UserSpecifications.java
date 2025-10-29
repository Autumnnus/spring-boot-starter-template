package com.autumnus.spring_boot_starter_template.modules.users.repository;

import com.autumnus.spring_boot_starter_template.modules.users.entity.RoleName;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> withRole(RoleName role) {
        return (root, query, cb) -> {
            if (role == null) {
                return cb.conjunction();
            }
            final var assignments = root.join("roleAssignments");
            final var roleJoin = assignments.join("role");
            return cb.equal(roleJoin.get("name"), role);
        };
    }

    public static Specification<User> withActive(Boolean active) {
        return (root, query, cb) -> active != null ? cb.equal(root.get("active"), active) : cb.conjunction();
    }
}
