package com.autumnus.spring_boot_starter_template.modules.users.service;

import com.autumnus.spring_boot_starter_template.common.exception.DomainException;

public class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException(String message) {
        super("USER_ALREADY_EXISTS", message);
    }
}
