package com.autumnus.spring_boot_starter_template.common.storage.model;

public record MediaVariantDefinition(MediaVariant variant, int maxWidth, int maxHeight, float quality) {
}
