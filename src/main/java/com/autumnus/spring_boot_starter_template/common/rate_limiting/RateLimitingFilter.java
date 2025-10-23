package com.autumnus.spring_boot_starter_template.common.rate_limiting;

import com.autumnus.spring_boot_starter_template.common.config.RateLimitingProperties;
import com.autumnus.spring_boot_starter_template.common.context.RequestContextHolder;
import com.autumnus.spring_boot_starter_template.common.exception.ApiError;
import com.autumnus.spring_boot_starter_template.common.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;
    private final ObjectMapper objectMapper;
    private final RateLimitingProperties properties;

    public RateLimitingFilter(
            RateLimitingService rateLimitingService,
            ObjectMapper objectMapper,
            RateLimitingProperties properties
    ) {
        this.rateLimitingService = rateLimitingService;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final String key = buildKey(request);
        if (!rateLimitingService.tryConsume(key)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(properties.getRefillPeriod().toSeconds()));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            final String traceId = RequestContextHolder.getContext().getTraceId();
            final ApiError payload = ApiError.of("RATE_LIMIT_EXCEEDED", "Too many requests", traceId);
            response.getWriter().write(objectMapper.writeValueAsString(payload));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String buildKey(HttpServletRequest request) {
        final String userId = SecurityUtils.getCurrentUserId().orElse("anonymous");
        final String ip = request.getRemoteAddr();
        final String deviceId = request.getHeader("X-Device-Id");
        return String.join(":", userId, ip, deviceId != null ? deviceId : "unknown", request.getMethod(), request.getRequestURI());
    }
}
