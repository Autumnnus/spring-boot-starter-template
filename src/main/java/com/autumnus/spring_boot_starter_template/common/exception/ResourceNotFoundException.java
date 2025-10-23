package com.autumnus.spring_boot_starter_template.common.exception;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }
}
