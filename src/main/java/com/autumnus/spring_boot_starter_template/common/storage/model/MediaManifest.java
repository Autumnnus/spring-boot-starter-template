package com.autumnus.spring_boot_starter_template.common.storage.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MediaManifest(
        MediaFileDescriptor original,
        Map<MediaVariant, MediaFileDescriptor> variants
) {
}
