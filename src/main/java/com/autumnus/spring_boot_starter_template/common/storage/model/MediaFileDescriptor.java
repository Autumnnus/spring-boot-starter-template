package com.autumnus.spring_boot_starter_template.common.storage.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MediaFileDescriptor(
        String key,
        String url,
        String contentType,
        long size
) {
}
