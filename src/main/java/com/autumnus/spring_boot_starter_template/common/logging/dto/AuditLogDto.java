package com.autumnus.spring_boot_starter_template.common.logging.dto;

import com.autumnus.spring_boot_starter_template.common.logging.annotation.AuditAction;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class AuditLogDto {

    Long userId;
    String entityType;
    String entityId;
    AuditAction action;
    JsonNode oldValue;
    JsonNode newValue;
    String ipAddress;
    String userAgent;
    String requestId;
    Instant createdAt;
    Long createdBy;
}
