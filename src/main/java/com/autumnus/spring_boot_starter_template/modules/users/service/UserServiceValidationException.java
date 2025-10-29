package com.autumnus.spring_boot_starter_template.modules.users.service;

import com.autumnus.spring_boot_starter_template.common.exception.DomainException;

public class UserServiceValidationException extends DomainException {

    public UserServiceValidationException(String code, String message) {
        super(code, message);
    }
}
