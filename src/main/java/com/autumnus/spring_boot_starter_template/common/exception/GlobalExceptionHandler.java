package com.autumnus.spring_boot_starter_template.common.exception;

import com.autumnus.spring_boot_starter_template.common.context.RequestContextHolder;
import com.autumnus.spring_boot_starter_template.common.idempotency.IdempotencyKeyConflictException;
import com.autumnus.spring_boot_starter_template.common.rate_limiting.RateLimitExceededException;
import com.autumnus.spring_boot_starter_template.common.security.UnauthorizedException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(ex.getCode(), ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IdempotencyKeyConflictException.class)
    public ResponseEntity<ApiError> handleIdempotencyConflict(IdempotencyKeyConflictException ex) {
        return buildResponse(ex.getCode(), ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiError> handleRateLimitExceeded(RateLimitExceededException ex) {
        return buildResponse(ex.getCode(), ex.getMessage(), HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex) {
        return buildResponse(ex.getCode(), ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse("FORBIDDEN", ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomainException(DomainException ex) {
        return buildResponse(ex.getCode(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        final String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Validation failed");
        return buildResponse("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        final String message = ex.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .orElse("Validation failed");
        return buildResponse("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse("INVALID_ARGUMENT", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        return buildResponse("INTERNAL_SERVER_ERROR", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiError> buildResponse(String code, String message, HttpStatus status) {
        final String traceId = RequestContextHolder.getContext().getTraceId();
        return ResponseEntity.status(status)
                .body(ApiError.of(code, message, traceId));
    }
}
