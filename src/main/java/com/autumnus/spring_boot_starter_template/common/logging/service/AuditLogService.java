package com.autumnus.spring_boot_starter_template.common.logging.service;

import com.autumnus.spring_boot_starter_template.common.logging.annotation.NoLog;
import com.autumnus.spring_boot_starter_template.common.logging.config.LoggingProperties;
import com.autumnus.spring_boot_starter_template.common.logging.dto.AuditLogDto;
import com.autumnus.spring_boot_starter_template.common.logging.entity.AuditLog;
import com.autumnus.spring_boot_starter_template.common.logging.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.Executor;

@Service
@NoLog
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final LoggingProperties loggingProperties;
    private final Executor loggingTaskExecutor;

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            LoggingProperties loggingProperties,
            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
            @Qualifier("loggingTaskExecutor") Executor loggingTaskExecutor
    ) {
        this.auditLogRepository = auditLogRepository;
        this.loggingProperties = loggingProperties;
        this.loggingTaskExecutor = loggingTaskExecutor;
    }

    public void save(AuditLogDto dto) {
        if (!loggingProperties.getAudit().isEnabled() || dto == null) {
            return;
        }
        final Runnable task = () -> auditLogRepository.save(mapToEntity(dto));
        if (loggingProperties.getAudit().isAsync()) {
            loggingTaskExecutor.execute(task);
        } else {
            task.run();
        }
    }

    private AuditLog mapToEntity(AuditLogDto dto) {
        final AuditLog log = new AuditLog();
        log.setUserId(dto.getUserId());
        log.setEntityType(dto.getEntityType());
        log.setEntityId(dto.getEntityId());
        if (dto.getAction() != null) {
            log.setAction(dto.getAction().name());
        }
        log.setOldValue(dto.getOldValue());
        log.setNewValue(dto.getNewValue());
        log.setIpAddress(dto.getIpAddress());
        log.setUserAgent(dto.getUserAgent());
        log.setRequestId(dto.getRequestId());
        log.setCreatedBy(dto.getCreatedBy());
        if (Objects.nonNull(dto.getCreatedAt())) {
            log.setCreatedAt(dto.getCreatedAt());
        } else {
            log.setCreatedAt(Instant.now());
        }
        return log;
    }
}
