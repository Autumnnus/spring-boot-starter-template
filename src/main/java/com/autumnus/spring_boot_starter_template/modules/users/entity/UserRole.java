package com.autumnus.spring_boot_starter_template.modules.users.entity;

public enum UserRole {
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_MODERATOR;

    public static UserRole fromString(String value) {
        return UserRole.valueOf(value.toUpperCase());
    }
}
