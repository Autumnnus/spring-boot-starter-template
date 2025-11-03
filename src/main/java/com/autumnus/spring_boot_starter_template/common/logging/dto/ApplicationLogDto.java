package com.autumnus.spring_boot_starter_template.common.logging.dto;

import com.autumnus.spring_boot_starter_template.common.logging.enums.LogLevel;
import lombok.*;

import java.time.Instant;

/**
 * DTO for application log data transfer
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationLogDto {

    private String id;
    private Instant timestamp;
    private LogLevel level;
    private String logger;
    private String thread;
    private String message;
    private String requestId;
    private Long userId;
    private String method;
    private String path;
    private Integer statusCode;
    private Long duration;
    private String ipAddress;
    private String userAgent;
    private ExceptionInfoDto exception;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExceptionInfoDto {
        private String className;
        private String message;
        private String stackTrace;
    }
}
