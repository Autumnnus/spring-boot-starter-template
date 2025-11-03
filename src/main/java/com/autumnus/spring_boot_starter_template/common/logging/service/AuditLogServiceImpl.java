package com.autumnus.spring_boot_starter_template.common.logging.service;

import com.autumnus.spring_boot_starter_template.common.context.RequestContextHolder;
import com.autumnus.spring_boot_starter_template.common.logging.dto.AuditLogDto;
import com.autumnus.spring_boot_starter_template.common.logging.entity.AuditLog;
import com.autumnus.spring_boot_starter_template.common.logging.enums.AuditAction;
import com.autumnus.spring_boot_starter_template.common.logging.enums.EntityType;
import com.autumnus.spring_boot_starter_template.common.logging.repository.AuditLogRepository;
import com.autumnus.spring_boot_starter_template.common.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for audit logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog save(AuditLog auditLog) {
        try {
            return auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
            return null;
        }
    }

    @Override
    @Async
    public AuditLog createAuditLog(EntityType entityType, String entityId, AuditAction action,
                                   Object oldValue, Object newValue) {
        try {
            var context = RequestContextHolder.getContext();
            var userId = SecurityUtils.getCurrentUserId().orElse(null);

            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .oldValue(serializeValue(oldValue))
                    .newValue(serializeValue(newValue))
                    .ipAddress(context.getIpAddress())
                    .requestId(context.getTraceId())
                    .createdAt(Instant.now())
                    .createdBy(userId)
                    .build();

            return save(auditLog);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getByUserId(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getByEntity(EntityType entityType, String entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getByAction(AuditAction action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDto> getByRequestId(String requestId) {
        return auditLogRepository.findByRequestId(requestId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getByDateRange(Instant startDate, Instant endDate, Pageable pageable) {
        return auditLogRepository.findByDateRange(startDate, endDate, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDto> getRecentByUser(Long userId) {
        return auditLogRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private String serializeValue(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize value: {}", value, e);
            return value.toString();
        }
    }

    private AuditLogDto convertToDto(AuditLog auditLog) {
        return modelMapper.map(auditLog, AuditLogDto.class);
    }
}
