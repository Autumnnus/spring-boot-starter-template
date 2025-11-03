package com.autumnus.spring_boot_starter_template.common.logging.service;

import com.autumnus.spring_boot_starter_template.common.logging.document.ApplicationLog;
import com.autumnus.spring_boot_starter_template.common.logging.dto.ApplicationLogDto;
import com.autumnus.spring_boot_starter_template.common.logging.enums.LogLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

/**
 * Service interface for application logging operations
 */
public interface ApplicationLogService {

    /**
     * Save an application log entry
     */
    void save(ApplicationLog log);

    /**
     * Create and save an application log entry
     */
    void createLog(LogLevel level, String message, String logger, Throwable exception);

    /**
     * Get logs by request ID
     */
    List<ApplicationLogDto> getByRequestId(String requestId);

    /**
     * Get logs by user ID
     */
    Page<ApplicationLogDto> getByUserId(Long userId, Pageable pageable);

    /**
     * Get logs by level
     */
    Page<ApplicationLogDto> getByLevel(LogLevel level, Pageable pageable);

    /**
     * Get error logs within a time range
     */
    Page<ApplicationLogDto> getErrorLogs(Instant start, Instant end, Pageable pageable);
}
