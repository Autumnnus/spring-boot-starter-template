package com.autumnus.spring_boot_starter_template.common.storage.dto;

import com.autumnus.spring_boot_starter_template.common.storage.model.MediaManifest;

public record MediaAsset(String basePath, String manifestKey, MediaManifest manifest) {
}
