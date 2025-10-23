package com.autumnus.spring_boot_starter_template.common.rate_limiting;

import com.autumnus.spring_boot_starter_template.common.exception.DomainException;

public class RateLimitExceededException extends DomainException {

    public RateLimitExceededException(String message) {
        super("RATE_LIMIT_EXCEEDED", message);
    }
}
