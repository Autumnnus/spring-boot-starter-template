package com.autumnus.spring_boot_starter_template.common.logging.context;

import java.util.Optional;

public final class AuditContextHolder {

    private static final ThreadLocal<AuditContext> CONTEXT =
            ThreadLocal.withInitial(AuditContext::new);

    private AuditContextHolder() {
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static void setEntityId(String entityId) {
        CONTEXT.get().setEntityId(entityId);
    }

    public static Optional<String> getEntityId() {
        return Optional.ofNullable(CONTEXT.get().getEntityId());
    }

    public static void setOldValue(Object oldValue) {
        CONTEXT.get().setOldValue(oldValue);
    }

    public static Optional<Object> getOldValue() {
        return Optional.ofNullable(CONTEXT.get().getOldValue());
    }

    public static void setNewValue(Object newValue) {
        CONTEXT.get().setNewValue(newValue);
    }

    public static Optional<Object> getNewValue() {
        return Optional.ofNullable(CONTEXT.get().getNewValue());
    }

    private static final class AuditContext {

        private String entityId;
        private Object oldValue;
        private Object newValue;

        String getEntityId() {
            return entityId;
        }

        void setEntityId(String entityId) {
            this.entityId = entityId;
        }

        Object getOldValue() {
            return oldValue;
        }

        void setOldValue(Object oldValue) {
            this.oldValue = oldValue;
        }

        Object getNewValue() {
            return newValue;
        }

        void setNewValue(Object newValue) {
            this.newValue = newValue;
        }
    }
}
