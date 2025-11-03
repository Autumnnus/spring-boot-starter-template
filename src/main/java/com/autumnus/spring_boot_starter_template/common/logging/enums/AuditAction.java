package com.autumnus.spring_boot_starter_template.common.logging.enums;

/**
 * Enum representing the type of action performed in an audit log
 */
public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    LOGIN,
    LOGOUT,
    LOGIN_FAILED,
    PASSWORD_CHANGE,
    PASSWORD_RESET,
    EMAIL_VERIFICATION,
    ROLE_ASSIGNMENT,
    PERMISSION_GRANT,
    PERMISSION_REVOKE,
    EXPORT,
    IMPORT,
    VIEW,
    SEARCH
}
