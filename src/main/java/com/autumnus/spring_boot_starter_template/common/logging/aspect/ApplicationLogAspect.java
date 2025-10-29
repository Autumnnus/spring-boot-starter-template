package com.autumnus.spring_boot_starter_template.common.logging.aspect;

import com.autumnus.spring_boot_starter_template.common.context.RequestContextHolder;
import com.autumnus.spring_boot_starter_template.common.logging.annotation.NoLog;
import com.autumnus.spring_boot_starter_template.common.logging.config.LoggingProperties;
import com.autumnus.spring_boot_starter_template.common.logging.dto.ApplicationLogDto;
import com.autumnus.spring_boot_starter_template.common.logging.service.ApplicationLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;

@Aspect
@Component
@NoLog
public class ApplicationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(ApplicationLogAspect.class);

    private final ApplicationLogService applicationLogService;
    private final LoggingProperties loggingProperties;

    public ApplicationLogAspect(
            ApplicationLogService applicationLogService,
            LoggingProperties loggingProperties
    ) {
        this.applicationLogService = applicationLogService;
        this.loggingProperties = loggingProperties;
    }

    @Around("execution(* com.autumnus.spring_boot_starter_template.modules..service..*(..))"
            + " && !@annotation(com.autumnus.spring_boot_starter_template.common.logging.annotation.NoLog)"
            + " && !@within(com.autumnus.spring_boot_starter_template.common.logging.annotation.NoLog)")
    public Object aroundServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!loggingProperties.getApplication().isEnabled()) {
            return joinPoint.proceed();
        }
        final Instant start = Instant.now();
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        log.debug("Entering {}", methodName);
        try {
            final Object result = joinPoint.proceed();
            recordLog("INFO", methodName + " completed successfully", start, null);
            log.debug("Exiting {}", methodName);
            return result;
        } catch (Throwable ex) {
            recordLog("ERROR", methodName + " failed: " + ex.getMessage(), start, ex);
            log.error("Error executing {}", methodName, ex);
            throw ex;
        }
    }

    private void recordLog(String level, String message, Instant start, Throwable throwable) {
        final Instant end = Instant.now();
        final long duration = Duration.between(start, end).toMillis();
        final var context = RequestContextHolder.getContext();
        final String requestId = context.getRequestId();
        final String path = context.getRequestUri();
        final String method = context.getHttpMethod();
        final String ipAddress = context.getIpAddress();
        final String userAgent = context.getUserAgent();
        final Long userId = parseLong(context.getUserId());
        final ApplicationLogDto.ExceptionInfo exceptionInfo = throwable == null
                ? null
                : ApplicationLogDto.ExceptionInfo.builder()
                .className(throwable.getClass().getName())
                .message(throwable.getMessage())
                .stackTrace(buildStackTrace(throwable))
                .build();
        final ApplicationLogDto dto = ApplicationLogDto.builder()
                .timestamp(start)
                .level(level)
                .logger(ApplicationLogAspect.class.getName())
                .thread(Thread.currentThread().getName())
                .message(message)
                .requestId(requestId)
                .userId(userId)
                .method(method)
                .path(path)
                .duration(duration)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .exception(exceptionInfo)
                .build();
        applicationLogService.record(dto);
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String buildStackTrace(Throwable throwable) {
        final StringBuilder builder = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            builder.append(element).append("\n");
        }
        return builder.toString();
    }
}
