package com.autumnus.spring_boot_starter_template.modules.users.entity;

import com.autumnus.spring_boot_starter_template.common.persistence.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "is_email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<UserRoleAssignment> roleAssignments = new HashSet<>();

    public Set<Role> getRoles() {
        final Set<Role> roles = new HashSet<>();
        for (UserRoleAssignment assignment : roleAssignments) {
            roles.add(assignment.getRole());
        }
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        roleAssignments.clear();
        if (roles != null) {
            for (Role role : roles) {
                addRole(role, null);
            }
        }
    }

    public void addRole(Role role, User assignedBy) {
        final UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setUser(this);
        assignment.setRole(role);
        assignment.setAssignedAt(Instant.now());
        assignment.setAssignedBy(assignedBy);
        roleAssignments.add(assignment);
    }
}
