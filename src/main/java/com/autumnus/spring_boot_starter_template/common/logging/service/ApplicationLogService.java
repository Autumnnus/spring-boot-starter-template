package com.autumnus.spring_boot_starter_template.common.logging.service;

import com.autumnus.spring_boot_starter_template.common.logging.annotation.NoLog;
import com.autumnus.spring_boot_starter_template.common.logging.config.LoggingProperties;
import com.autumnus.spring_boot_starter_template.common.logging.document.ApplicationLog;
import com.autumnus.spring_boot_starter_template.common.logging.dto.ApplicationLogDto;
import com.autumnus.spring_boot_starter_template.common.logging.repository.ApplicationLogRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executor;

@Service
@NoLog
public class ApplicationLogService {

    private final ApplicationLogRepository applicationLogRepository;
    private final LoggingProperties loggingProperties;
    private final Executor loggingTaskExecutor;

    public ApplicationLogService(
            ApplicationLogRepository applicationLogRepository,
            LoggingProperties loggingProperties,
            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
            @Qualifier("loggingTaskExecutor") Executor loggingTaskExecutor
    ) {
        this.applicationLogRepository = applicationLogRepository;
        this.loggingProperties = loggingProperties;
        this.loggingTaskExecutor = loggingTaskExecutor;
    }

    public void record(ApplicationLogDto dto) {
        if (!loggingProperties.getApplication().isEnabled() || dto == null) {
            return;
        }
        final Runnable task = () -> applicationLogRepository.save(mapToDocument(dto));
        if (loggingProperties.getApplication().isAsync()) {
            loggingTaskExecutor.execute(task);
        } else {
            task.run();
        }
    }

    private ApplicationLog mapToDocument(ApplicationLogDto dto) {
        final ApplicationLog.ExceptionDocument exceptionDocument = dto.getException() == null
                ? null
                : ApplicationLog.ExceptionDocument.builder()
                .className(dto.getException().getClassName())
                .message(dto.getException().getMessage())
                .stackTrace(dto.getException().getStackTrace())
                .build();
        return ApplicationLog.builder()
                .id(UUID.randomUUID().toString())
                .timestamp(dto.getTimestamp() != null ? dto.getTimestamp() : Instant.now())
                .level(dto.getLevel())
                .logger(dto.getLogger())
                .thread(dto.getThread())
                .message(dto.getMessage())
                .requestId(dto.getRequestId())
                .userId(dto.getUserId())
                .method(dto.getMethod())
                .path(dto.getPath())
                .statusCode(dto.getStatusCode())
                .duration(dto.getDuration())
                .ipAddress(dto.getIpAddress())
                .userAgent(dto.getUserAgent())
                .exception(exceptionDocument)
                .build();
    }
}
