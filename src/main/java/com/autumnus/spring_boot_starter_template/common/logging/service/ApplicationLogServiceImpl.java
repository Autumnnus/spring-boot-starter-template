package com.autumnus.spring_boot_starter_template.common.logging.service;

import com.autumnus.spring_boot_starter_template.common.context.RequestContextHolder;
import com.autumnus.spring_boot_starter_template.common.logging.document.ApplicationLog;
import com.autumnus.spring_boot_starter_template.common.logging.dto.ApplicationLogDto;
import com.autumnus.spring_boot_starter_template.common.logging.enums.LogLevel;
import com.autumnus.spring_boot_starter_template.common.logging.repository.ApplicationLogRepository;
import com.autumnus.spring_boot_starter_template.common.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for application logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationLogServiceImpl implements ApplicationLogService {

    private final ApplicationLogRepository applicationLogRepository;
    private final ModelMapper modelMapper;

    @Override
    @Async
    public void save(ApplicationLog appLog) {
        try {
            applicationLogRepository.save(appLog);
        } catch (Exception e) {
            log.error("Failed to save application log to Elasticsearch", e);
        }
    }

    @Override
    @Async
    public void createLog(LogLevel level, String message, String logger, Throwable exception) {
        try {
            var context = RequestContextHolder.getContext();
            var userId = SecurityUtils.getCurrentUserId().orElse(null);

            ApplicationLog.ApplicationLogBuilder builder = ApplicationLog.builder()
                    .timestamp(Instant.now())
                    .level(level)
                    .logger(logger)
                    .thread(Thread.currentThread().getName())
                    .message(message)
                    .requestId(context.getTraceId())
                    .userId(userId)
                    .ipAddress(context.getIpAddress());

            if (exception != null) {
                ApplicationLog.ExceptionInfo exceptionInfo = ApplicationLog.ExceptionInfo.builder()
                        .className(exception.getClass().getName())
                        .message(exception.getMessage())
                        .stackTrace(getStackTrace(exception))
                        .build();
                builder.exception(exceptionInfo);
            }

            save(builder.build());
        } catch (Exception e) {
            log.error("Failed to create application log", e);
        }
    }

    @Override
    public List<ApplicationLogDto> getByRequestId(String requestId) {
        return applicationLogRepository.findByRequestId(requestId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ApplicationLogDto> getByUserId(Long userId, Pageable pageable) {
        return applicationLogRepository.findByUserId(userId, pageable)
                .map(this::convertToDto);
    }

    @Override
    public Page<ApplicationLogDto> getByLevel(LogLevel level, Pageable pageable) {
        return applicationLogRepository.findByLevel(level, pageable)
                .map(this::convertToDto);
    }

    @Override
    public Page<ApplicationLogDto> getErrorLogs(Instant start, Instant end, Pageable pageable) {
        List<LogLevel> errorLevels = Arrays.asList(LogLevel.ERROR, LogLevel.FATAL);
        return applicationLogRepository.findByLevelIn(errorLevels, pageable)
                .map(this::convertToDto);
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    private ApplicationLogDto convertToDto(ApplicationLog log) {
        return modelMapper.map(log, ApplicationLogDto.class);
    }
}
