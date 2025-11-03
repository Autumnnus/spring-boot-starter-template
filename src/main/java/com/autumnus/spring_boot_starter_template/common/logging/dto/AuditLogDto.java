package com.autumnus.spring_boot_starter_template.common.logging.dto;

import com.autumnus.spring_boot_starter_template.common.logging.enums.AuditAction;
import com.autumnus.spring_boot_starter_template.common.logging.enums.EntityType;
import lombok.*;

import java.time.Instant;

/**
 * DTO for audit log data transfer
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDto {

    private Long id;
    private Long userId;
    private EntityType entityType;
    private String entityId;
    private AuditAction action;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String userAgent;
    private String requestId;
    private Instant createdAt;
    private Long createdBy;
}
