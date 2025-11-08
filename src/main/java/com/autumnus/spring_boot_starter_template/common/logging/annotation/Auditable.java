package com.autumnus.spring_boot_starter_template.common.logging.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    String entityType();

    AuditAction action();

    boolean captureOldValue() default false;

    boolean captureNewValue() default true;

    /**
     * Optional SpEL expression used to resolve the entity identifier from method arguments.
     */
    String entityIdExpression() default "";
}
