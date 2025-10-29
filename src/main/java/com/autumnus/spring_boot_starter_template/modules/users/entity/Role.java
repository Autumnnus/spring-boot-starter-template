package com.autumnus.spring_boot_starter_template.modules.users.entity;

import com.autumnus.spring_boot_starter_template.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "roles")
    private Set<Permission> permissions = new HashSet<>();
}
