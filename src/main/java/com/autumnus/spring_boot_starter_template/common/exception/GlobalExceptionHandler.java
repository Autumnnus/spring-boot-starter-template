package com.autumnus.spring_boot_starter_template.common.exception;

import com.autumnus.spring_boot_starter_template.common.context.RequestContextHolder;
import com.autumnus.spring_boot_starter_template.common.i18n.MessageService;
import com.autumnus.spring_boot_starter_template.common.storage.exception.MediaStorageException;
import com.autumnus.spring_boot_starter_template.common.idempotency.IdempotencyKeyConflictException;
import com.autumnus.spring_boot_starter_template.common.rate_limiting.RateLimitExceededException;
import com.autumnus.spring_boot_starter_template.common.security.UnauthorizedException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for all REST controllers.
 * Provides internationalized error messages using MessageService.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageService messageService;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        String message = getLocalizedMessage(ex);
        return buildResponse(ex.getCode(), message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IdempotencyKeyConflictException.class)
    public ResponseEntity<ApiError> handleIdempotencyConflict(IdempotencyKeyConflictException ex) {
        String message = messageService.getMessageOrDefault("idempotency.conflict", ex.getMessage());
        return buildResponse(ex.getCode(), message, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiError> handleRateLimitExceeded(RateLimitExceededException ex) {
        String message = messageService.getMessageOrDefault("rate_limit.exceeded", ex.getMessage());
        return buildResponse(ex.getCode(), message, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex) {
        String message = messageService.getMessageOrDefault("error.unauthorized", ex.getMessage());
        return buildResponse(ex.getCode(), message, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MediaStorageException.class)
    public ResponseEntity<ApiError> handleMediaStorage(MediaStorageException ex) {
        String message = getLocalizedMessage(ex);
        return buildResponse(ex.getCode(), message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        String message = messageService.getMessageOrDefault("error.forbidden", ex.getMessage());
        return buildResponse("FORBIDDEN", message, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomainException(DomainException ex) {
        String message = getLocalizedMessage(ex);
        return buildResponse(ex.getCode(), message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        final String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse(messageService.getMessage("error.validation_error"));
        return buildResponse("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        final String message = ex.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .orElse(messageService.getMessage("error.validation_error"));
        return buildResponse("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        String message = messageService.getMessageOrDefault("error.invalid_argument", ex.getMessage());
        return buildResponse("INVALID_ARGUMENT", message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        String message = messageService.getMessageOrDefault("error.internal_server_error", ex.getMessage());
        return buildResponse("INTERNAL_SERVER_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiError> buildResponse(String code, String message, HttpStatus status) {
        final String traceId = RequestContextHolder.getContext().getTraceId();
        return ResponseEntity.status(status)
                .body(ApiError.of(code, message, traceId));
    }

    /**
     * Gets localized message from exception or falls back to default message.
     * If exception message contains a message key (e.g., "user.not_found"),
     * it will be resolved from message properties.
     *
     * @param ex the exception
     * @return localized message
     */
    private String getLocalizedMessage(Exception ex) {
        String originalMessage = ex.getMessage();
        if (originalMessage == null) {
            return messageService.getMessage("error.internal_server_error");
        }
        // Try to get localized message, fall back to original message
        return messageService.getMessageOrDefault(originalMessage, originalMessage);
    }
}
