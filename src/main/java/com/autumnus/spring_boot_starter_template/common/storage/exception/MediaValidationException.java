package com.autumnus.spring_boot_starter_template.common.storage.exception;

import com.autumnus.spring_boot_starter_template.common.exception.DomainException;

public class MediaValidationException extends DomainException {

    public MediaValidationException(String message) {
        super("MEDIA_VALIDATION_ERROR", message);
    }
}
