package com.autumnus.spring_boot_starter_template.common.logging.aspect;

import com.autumnus.spring_boot_starter_template.common.context.RequestContextHolder;
import com.autumnus.spring_boot_starter_template.common.logging.annotation.Auditable;
import com.autumnus.spring_boot_starter_template.common.logging.annotation.NoLog;
import com.autumnus.spring_boot_starter_template.common.logging.config.LoggingProperties;
import com.autumnus.spring_boot_starter_template.common.logging.context.AuditContextHolder;
import com.autumnus.spring_boot_starter_template.common.logging.dto.AuditLogDto;
import com.autumnus.spring_boot_starter_template.common.logging.service.AuditLogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Optional;

@Aspect
@Component
@NoLog
public class AuditLogAspect {

    private final AuditLogService auditLogService;
    private final LoggingProperties loggingProperties;
    private final ObjectMapper objectMapper;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    public AuditLogAspect(
            AuditLogService auditLogService,
            LoggingProperties loggingProperties,
            ObjectMapper objectMapper
    ) {
        this.auditLogService = auditLogService;
        this.loggingProperties = loggingProperties;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(auditable)")
    public Object aroundAuditableMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        if (!loggingProperties.getAudit().isEnabled()) {
            try {
                return joinPoint.proceed();
            } finally {
                AuditContextHolder.clear();
            }
        }
        AuditContextHolder.clear();
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Object[] args = joinPoint.getArgs();
        String entityId = resolveEntityId(signature, args, auditable);
        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            throw ex;
        } finally {
            try {
                final String requestId = RequestContextHolder.getContext().getRequestId();
                final String ipAddress = RequestContextHolder.getContext().getIpAddress();
                final String userAgent = RequestContextHolder.getContext().getUserAgent();
                final Long userId = parseLong(RequestContextHolder.getContext().getUserId());
                final JsonNode oldValue = auditable.captureOldValue()
                        ? AuditContextHolder.getOldValue().map(this::convertToJson).orElse(null)
                        : null;
                final JsonNode newValue = auditable.captureNewValue()
                        ? resolveNewValue(result)
                        : null;
                entityId = resolveEntityIdFallback(entityId, result);
                final AuditLogDto dto = AuditLogDto.builder()
                        .entityType(auditable.entityType())
                        .action(auditable.action())
                        .entityId(entityId)
                        .userId(userId)
                        .oldValue(oldValue)
                        .newValue(newValue)
                        .ipAddress(ipAddress)
                        .userAgent(userAgent)
                        .requestId(requestId)
                        .createdAt(Instant.now())
                        .createdBy(userId)
                        .build();
                auditLogService.save(dto);
            } finally {
                AuditContextHolder.clear();
            }
        }
    }

    private JsonNode convertToJson(Object value) {
        return value == null ? null : objectMapper.valueToTree(value);
    }

    private JsonNode resolveNewValue(Object result) {
        final Optional<Object> contextValue = AuditContextHolder.getNewValue();
        if (contextValue.isPresent()) {
            return convertToJson(contextValue.get());
        }
        return convertToJson(result);
    }

    private String resolveEntityId(MethodSignature signature, Object[] args, Auditable auditable) {
        if (StringUtils.hasText(auditable.entityIdExpression())) {
            final EvaluationContext context = buildEvaluationContext(signature, args);
            final Expression expression = expressionParser.parseExpression(auditable.entityIdExpression());
            final Object value = expression.getValue(context);
            return value != null ? value.toString() : null;
        }
        final String[] parameterNames = signature.getParameterNames();
        if (parameterNames == null) {
            return null;
        }
        for (int i = 0; i < parameterNames.length; i++) {
            final String parameterName = parameterNames[i];
            if (parameterName == null) {
                continue;
            }
            if (parameterName.equalsIgnoreCase("id") || parameterName.toLowerCase().endsWith("id")) {
                final Object value = args[i];
                if (value != null) {
                    return value.toString();
                }
            }
        }
        return null;
    }

    private EvaluationContext buildEvaluationContext(MethodSignature signature, Object[] args) {
        final StandardEvaluationContext context = new StandardEvaluationContext();
        final String[] parameterNames = signature.getParameterNames();
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        return context;
    }

    private String resolveEntityIdFallback(String existing, Object result) {
        if (StringUtils.hasText(existing)) {
            return existing;
        }
        return AuditContextHolder.getEntityId().orElseGet(() -> extractIdFromResult(result));
    }

    private String extractIdFromResult(Object result) {
        if (result == null) {
            return null;
        }
        try {
            final Object idValue = result.getClass().getMethod("getId").invoke(result);
            return idValue != null ? idValue.toString() : null;
        } catch (Exception ignored) {
        }
        return null;
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
}
