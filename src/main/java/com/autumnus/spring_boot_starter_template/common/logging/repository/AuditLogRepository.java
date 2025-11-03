package com.autumnus.spring_boot_starter_template.common.logging.repository;

import com.autumnus.spring_boot_starter_template.common.logging.entity.AuditLog;
import com.autumnus.spring_boot_starter_template.common.logging.enums.AuditAction;
import com.autumnus.spring_boot_starter_template.common.logging.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for AuditLog entity
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find audit logs by user ID
     */
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    /**
     * Find audit logs by entity type and entity ID
     */
    Page<AuditLog> findByEntityTypeAndEntityId(EntityType entityType, String entityId, Pageable pageable);

    /**
     * Find audit logs by action
     */
    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    /**
     * Find audit logs by request ID
     */
    List<AuditLog> findByRequestId(String requestId);

    /**
     * Find audit logs within a date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<AuditLog> findByDateRange(@Param("startDate") Instant startDate,
                                   @Param("endDate") Instant endDate,
                                   Pageable pageable);

    /**
     * Find recent audit logs for a user
     */
    List<AuditLog> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find audit logs by entity and action
     */
    Page<AuditLog> findByEntityTypeAndAction(EntityType entityType, AuditAction action, Pageable pageable);
}
