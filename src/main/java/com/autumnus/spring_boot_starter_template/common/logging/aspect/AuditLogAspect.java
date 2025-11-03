package com.autumnus.spring_boot_starter_template.common.logging.aspect;

import com.autumnus.spring_boot_starter_template.common.context.RequestContextHolder;
import com.autumnus.spring_boot_starter_template.common.logging.annotation.Auditable;
import com.autumnus.spring_boot_starter_template.common.logging.entity.AuditLog;
import com.autumnus.spring_boot_starter_template.common.logging.service.AuditLogService;
import com.autumnus.spring_boot_starter_template.common.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Aspect for auditing method executions annotated with @Auditable
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object oldValue = null;
        Object result;

        try {
            // Extract entity ID using SpEL
            String entityId = extractEntityId(joinPoint, auditable.entityIdExpression());

            // Capture old value if needed (for UPDATE/DELETE operations)
            if (auditable.captureOldValue()) {
                oldValue = captureOldValue(joinPoint);
            }

            // Execute the method
            result = joinPoint.proceed();

            // Capture new value (result of the method)
            Object newValue = result;

            // Create audit log
            createAuditLog(auditable, entityId, oldValue, newValue);

        } catch (Exception e) {
            log.error("Error in audit aspect", e);
            throw e;
        }

        return result;
    }

    private String extractEntityId(ProceedingJoinPoint joinPoint, String expression) {
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();

            // Get method parameters
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            // Add parameters to context
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }

            // Evaluate expression
            Object value = parser.parseExpression(expression).getValue(context);
            return value != null ? value.toString() : "unknown";
        } catch (Exception e) {
            log.warn("Failed to extract entity ID using expression: {}", expression, e);
            return "unknown";
        }
    }

    private Object captureOldValue(ProceedingJoinPoint joinPoint) {
        // This is a simplified version
        // In a real implementation, you would fetch the entity from database before update
        return null;
    }

    private void createAuditLog(Auditable auditable, String entityId, Object oldValue, Object newValue) {
        try {
            var context = RequestContextHolder.getContext();
            var userId = SecurityUtils.getCurrentUserId().orElse(null);

            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .entityType(auditable.entityType())
                    .entityId(entityId)
                    .action(auditable.action())
                    .oldValue(serializeValue(oldValue))
                    .newValue(serializeValue(newValue))
                    .ipAddress(context.getIpAddress())
                    .requestId(context.getTraceId())
                    .createdAt(Instant.now())
                    .createdBy(userId)
                    .build();

            auditLogService.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    private String serializeValue(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.warn("Failed to serialize value", e);
            return value.toString();
        }
    }
}
