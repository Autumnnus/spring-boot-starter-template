package com.autumnus.spring_boot_starter_template.modules.auth.service;

import com.autumnus.spring_boot_starter_template.common.exception.DomainException;

public class TokenValidationException extends DomainException {

    public TokenValidationException(String code, String message) {
        super(code, message);
    }
}
