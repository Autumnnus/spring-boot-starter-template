package com.autumnus.spring_boot_starter_template.common.logging.annotation;

import com.autumnus.spring_boot_starter_template.common.logging.enums.AuditAction;
import com.autumnus.spring_boot_starter_template.common.logging.enums.EntityType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be audited.
 * When applied, the method execution will be logged in the audit_logs table.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * The type of entity being audited (USER, PRODUCT, ORDER, etc.)
     */
    EntityType entityType();

    /**
     * The action being performed (CREATE, UPDATE, DELETE, LOGIN, etc.)
     */
    AuditAction action();

    /**
     * Whether to capture the old value (for UPDATE/DELETE operations)
     */
    boolean captureOldValue() default false;

    /**
     * SpEL expression to extract entity ID from method parameters
     * Example: "#id" or "#request.id"
     */
    String entityIdExpression() default "#id";
}
