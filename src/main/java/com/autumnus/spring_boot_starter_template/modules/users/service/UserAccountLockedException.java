package com.autumnus.spring_boot_starter_template.modules.users.service;

import com.autumnus.spring_boot_starter_template.common.exception.DomainException;

public class UserAccountLockedException extends DomainException {

    public UserAccountLockedException(String message) {
        super(message);
    }
}
