package com.autumnus.spring_boot_starter_template.common.idempotency;

import com.autumnus.spring_boot_starter_template.common.context.RequestContextHolder;
import com.autumnus.spring_boot_starter_template.common.exception.ApiError;
import com.autumnus.spring_boot_starter_template.common.i18n.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.LocaleResolver;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class IdempotencyKeyFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/api/v1/auth",
            "/api/v1/i18n",
            "/login/oauth2",
            "/oauth2",
            "/oauth-test.html",
            "/css",
            "/js",
            "/images"
    );

    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final IdempotencyKeyValidator keyValidator;
    private final LocaleResolver localeResolver;

    public IdempotencyKeyFilter(
            ObjectMapper objectMapper,
            MessageService messageService,
            IdempotencyKeyValidator keyValidator,
            LocaleResolver localeResolver
    ) {
        this.objectMapper = objectMapper;
        this.messageService = messageService;
        this.keyValidator = keyValidator;
        this.localeResolver = localeResolver;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // Set locale from Accept-Language header for i18n support
        final Locale locale = localeResolver.resolveLocale(request);
        LocaleContextHolder.setLocale(locale);
        
        final String path = request.getRequestURI();
        
        // Skip check for excluded paths
        if (isExcludedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if Idempotency-Key header is present
        final String idempotencyKey = request.getHeader(IDEMPOTENCY_HEADER);
        if (!StringUtils.hasText(idempotencyKey)) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            final String traceId = RequestContextHolder.getContext().getTraceId();
            final ApiError payload = ApiError.of(
                    "IDEMPOTENCY_KEY_REQUIRED",
                    messageService.getMessage("idempotency.key_required"),
                    traceId
            );
            response.getWriter().write(objectMapper.writeValueAsString(payload));
            return;
        }

        // Check if the key has already been used
        final Boolean wasMarked = keyValidator.tryMarkAsUsed(idempotencyKey).block();
        if (Boolean.FALSE.equals(wasMarked)) {
            response.setStatus(HttpStatus.CONFLICT.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            final String traceId = RequestContextHolder.getContext().getTraceId();
            final ApiError payload = ApiError.of(
                    "IDEMPOTENCY_KEY_ALREADY_USED",
                    messageService.getMessage("idempotency.key_already_used"),
                    traceId
            );
            response.getWriter().write(objectMapper.writeValueAsString(payload));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }
}
