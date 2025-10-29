package com.autumnus.spring_boot_starter_template.common.logging.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class ApplicationLogDto {

    Instant timestamp;
    String level;
    String logger;
    String thread;
    String message;
    String requestId;
    Long userId;
    String method;
    String path;
    Integer statusCode;
    Long duration;
    String ipAddress;
    String userAgent;
    ExceptionInfo exception;

    @Value
    @Builder
    public static class ExceptionInfo {
        String className;
        String message;
        String stackTrace;
    }
}
