package com.autumnus.spring_boot_starter_template.common.logging.service;

import com.autumnus.spring_boot_starter_template.common.logging.dto.AuditLogDto;
import com.autumnus.spring_boot_starter_template.common.logging.entity.AuditLog;
import com.autumnus.spring_boot_starter_template.common.logging.enums.AuditAction;
import com.autumnus.spring_boot_starter_template.common.logging.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

/**
 * Service interface for audit logging operations
 */
public interface AuditLogService {

    /**
     * Save an audit log entry
     */
    AuditLog save(AuditLog auditLog);

    /**
     * Create and save an audit log entry
     */
    AuditLog createAuditLog(EntityType entityType, String entityId, AuditAction action,
                           Object oldValue, Object newValue);

    /**
     * Get audit logs by user ID
     */
    Page<AuditLogDto> getByUserId(Long userId, Pageable pageable);

    /**
     * Get audit logs by entity
     */
    Page<AuditLogDto> getByEntity(EntityType entityType, String entityId, Pageable pageable);

    /**
     * Get audit logs by action
     */
    Page<AuditLogDto> getByAction(AuditAction action, Pageable pageable);

    /**
     * Get audit logs by request ID
     */
    List<AuditLogDto> getByRequestId(String requestId);

    /**
     * Get audit logs within a date range
     */
    Page<AuditLogDto> getByDateRange(Instant startDate, Instant endDate, Pageable pageable);

    /**
     * Get recent audit logs for a user
     */
    List<AuditLogDto> getRecentByUser(Long userId);
}
