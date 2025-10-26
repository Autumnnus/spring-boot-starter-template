package com.autumnus.spring_boot_starter_template.modules.users.entity;

public enum UserRole {
    USER,
    ADMIN,
    MODERATOR;

    public static UserRole fromString(String value) {
        return UserRole.valueOf(value.toUpperCase());
    }
}
