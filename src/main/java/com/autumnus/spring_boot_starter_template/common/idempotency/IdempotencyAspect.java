package com.autumnus.spring_boot_starter_template.common.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.stream.Stream;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.data.domain.Pageable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Aspect
@Component
public class IdempotencyAspect {

    public static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    public IdempotencyAspect(IdempotencyService idempotencyService, ObjectMapper objectMapper) {
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(idempotent)")
    public Object handleIdempotent(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        final String idempotencyKey = resolveIdempotencyKey();
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }
        final String requestHash = buildRequestHash(joinPoint);
        final Optional<IdempotencyKey> existingRecord = idempotencyService.findByKey(idempotencyKey);
        if (existingRecord.isPresent()) {
            final IdempotencyKey record = existingRecord.get();
            if (!record.getRequestHash().equals(requestHash)) {
                throw new IdempotencyKeyConflictException("Idempotency key has already been used with a different payload");
            }
            return deserializeResponse(joinPoint, record);
        }

        final Object result = joinPoint.proceed();
        final PersistableResponse persistableResponse = PersistableResponse.from(result, objectMapper);
        final Instant expiresAt = Instant.now().plusSeconds(idempotent.ttlSeconds());
        idempotencyService.save(
                idempotencyKey,
                requestHash,
                persistableResponse.responseBody(),
                persistableResponse.statusCode(),
                expiresAt);
        return result;
    }

    private Object deserializeResponse(ProceedingJoinPoint joinPoint, IdempotencyKey record) throws JsonProcessingException {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Class<?> returnType = signature.getReturnType();
        if (ResponseEntity.class.isAssignableFrom(returnType)) {
            return ResponseEntity.status(record.getStatusCode())
                    .body(objectMapper.readValue(record.getResponseBody(), Object.class));
        }
        return objectMapper.readValue(record.getResponseBody(), returnType);
    }

    private String resolveIdempotencyKey() {
        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest().getHeader(IDEMPOTENCY_HEADER);
        }
        return null;
    }

    private String buildRequestHash(ProceedingJoinPoint joinPoint) {
        try {
            final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            final String payload = signature.getDeclaringTypeName()
                    + "#"
                    + signature.getMethod().getName()
                    + objectMapper.writeValueAsString(Stream.of(joinPoint.getArgs())
                            .map(this::normalizeArgument)
                            .toList());
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (JsonProcessingException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Failed to create idempotency hash", ex);
        }
    }

    private Object normalizeArgument(Object argument) {
        if (argument instanceof Pageable pageable) {
            return new PageableFingerprint(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().toString());
        }
        if (argument instanceof BindingResult || argument instanceof HttpServletRequest || argument instanceof HttpServletResponse) {
            return null;
        }
        return argument;
    }

    private record PersistableResponse(String responseBody, int statusCode) {

        static PersistableResponse from(Object result, ObjectMapper objectMapper) throws JsonProcessingException {
            if (result instanceof ResponseEntity<?> responseEntity) {
                final Object body = responseEntity.getBody();
                final String serializedBody = objectMapper.writeValueAsString(body);
                return new PersistableResponse(serializedBody, responseEntity.getStatusCode().value());
            }
            return new PersistableResponse(objectMapper.writeValueAsString(result), 200);
        }
    }

    private record PageableFingerprint(int page, int size, String sort) {}
}
