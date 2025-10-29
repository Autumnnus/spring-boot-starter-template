package com.autumnus.spring_boot_starter_template.modules.users.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "user_roles")
@Getter
@Setter
public class UserRoleAssignment {

    @EmbeddedId
    private UserRoleAssignmentId id = new UserRoleAssignmentId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @Getter
    @Setter
    @Embeddable
    public static class UserRoleAssignmentId implements Serializable {

        private Long userId;
        private Long roleId;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            UserRoleAssignmentId that = (UserRoleAssignmentId) o;
            return java.util.Objects.equals(userId, that.userId)
                    && java.util.Objects.equals(roleId, that.roleId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(userId, roleId);
        }
    }
}
