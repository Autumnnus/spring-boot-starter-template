package com.autumnus.spring_boot_starter_template.common.idempotency;

import com.autumnus.spring_boot_starter_template.common.exception.DomainException;

public class IdempotencyKeyConflictException extends DomainException {

    public IdempotencyKeyConflictException(String message) {
        super("IDEMPOTENCY_KEY_CONFLICT", message);
    }
}
