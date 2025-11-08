package com.autumnus.spring_boot_starter_template.common.storage.model;

public record MediaUpload(String originalFilename, String contentType, byte[] content) {

    public long size() {
        return content == null ? 0 : content.length;
    }
}
