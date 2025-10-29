package com.autumnus.spring_boot_starter_template.modules.users.entity;

import com.autumnus.spring_boot_starter_template.common.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "permissions",
        indexes = {@Index(name = "uk_permissions_name", columnList = "name", unique = true)}
)
@Getter
@Setter
public class Permission extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String resource;

    @Column(nullable = false)
    private String action;

    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();
}
