package com.autumnus.spring_boot_starter_template.common.storage.exception;

import com.autumnus.spring_boot_starter_template.common.exception.DomainException;

public class MediaStorageException extends DomainException {

    public MediaStorageException(String message) {
        super("MEDIA_STORAGE_ERROR", message);
    }

    public MediaStorageException(String message, Throwable cause) {
        super("MEDIA_STORAGE_ERROR", message);
        initCause(cause);
    }
}
