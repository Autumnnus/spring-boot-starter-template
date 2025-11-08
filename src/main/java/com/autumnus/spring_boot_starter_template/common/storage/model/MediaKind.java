package com.autumnus.spring_boot_starter_template.common.storage.model;

import com.autumnus.spring_boot_starter_template.common.storage.exception.MediaValidationException;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public enum MediaKind {
    IMAGE(Set.of("image/jpeg", "image/png"), 10 * 1024 * 1024L),
    VIDEO(Set.of("video/mp4", "video/quicktime"), 150 * 1024 * 1024L),
    AUDIO(Set.of("audio/mpeg", "audio/wav"), 20 * 1024 * 1024L),
    DOCUMENT(Set.of("application/pdf"), 25 * 1024 * 1024L);

    private static final Map<String, String> MIME_EXTENSION = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "application/pdf", "pdf",
            "video/mp4", "mp4",
            "video/quicktime", "mov",
            "audio/mpeg", "mp3",
            "audio/wav", "wav"
    );

    private final Set<String> allowedMimeTypes;
    private final long maxSize;

    MediaKind(Set<String> allowedMimeTypes, long maxSize) {
        this.allowedMimeTypes = allowedMimeTypes;
        this.maxSize = maxSize;
    }

    public void validate(String mimeType, long size) {
        if (mimeType == null || !allowedMimeTypes.contains(mimeType.toLowerCase(Locale.ROOT))) {
            throw new MediaValidationException("Unsupported MIME type: " + mimeType);
        }
        if (size > maxSize) {
            throw new MediaValidationException("File size exceeds the limit of " + maxSize + " bytes for " + name().toLowerCase(Locale.ROOT));
        }
    }

    public String resolveExtension(String originalFilename, String mimeType) {
        if (originalFilename != null && originalFilename.contains(".")) {
            final String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
            if (!extension.isBlank()) {
                return extension.toLowerCase(Locale.ROOT);
            }
        }
        final String normalizedMime = mimeType == null ? null : mimeType.toLowerCase(Locale.ROOT);
        if (normalizedMime != null && MIME_EXTENSION.containsKey(normalizedMime)) {
            return MIME_EXTENSION.get(normalizedMime);
        }
        throw new MediaValidationException("Unable to resolve file extension for MIME type: " + mimeType);
    }
}
