package com.autumnus.spring_boot_starter_template.modules.users.dto;

public record ProfilePhotoUploadCommand(String originalFilename, String contentType, byte[] content) {
}
