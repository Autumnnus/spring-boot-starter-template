package com.autumnus.spring_boot_starter_template.common.storage.dto;

import java.util.Map;

public record MediaResourceResponse(MediaFileResponse original, Map<String, MediaFileResponse> variants) {
}
