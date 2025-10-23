package com.autumnus.spring_boot_starter_template.modules.users.repository;

import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserRole;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> withEmail(String email) {
        return (root, query, cb) -> StringUtils.hasText(email)
                ? cb.equal(cb.lower(root.get("email")), email.toLowerCase())
                : cb.conjunction();
    }

    public static Specification<User> withStatus(UserStatus status) {
        return (root, query, cb) -> status != null ? cb.equal(root.get("status"), status) : cb.conjunction();
    }

    public static Specification<User> withRole(UserRole role) {
        return (root, query, cb) -> {
            if (role == null) {
                return cb.conjunction();
            }
            return cb.isMember(role, root.get("roles"));
        };
    }
}
