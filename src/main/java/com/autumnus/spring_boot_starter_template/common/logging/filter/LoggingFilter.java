package com.autumnus.spring_boot_starter_template.common.logging.filter;

import com.autumnus.spring_boot_starter_template.common.logging.document.ApplicationLog;
import com.autumnus.spring_boot_starter_template.common.logging.enums.LogLevel;
import com.autumnus.spring_boot_starter_template.common.logging.service.ApplicationLogService;
import com.autumnus.spring_boot_starter_template.common.security.SecurityUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.Instant;

/**
 * Filter for logging HTTP requests and responses
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
@Slf4j
public class LoggingFilter implements Filter {

    private final ApplicationLogService applicationLogService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Wrap request and response to read body multiple times
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);

        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequestResponse(requestWrapper, responseWrapper, duration);
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequestResponse(ContentCachingRequestWrapper request,
                                    ContentCachingResponseWrapper response,
                                    long duration) {
        try {
            String method = request.getMethod();
            String path = request.getRequestURI();
            int statusCode = response.getStatus();
            String requestId = MDC.get("traceId");
            Long userId = SecurityUtils.getCurrentUserId().orElse(null);
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            // Log to SLF4J
            log.info("HTTP {} {} - Status: {} - Duration: {}ms",
                    method, path, statusCode, duration);

            // Save to Elasticsearch
            ApplicationLog applicationLog = ApplicationLog.builder()
                    .timestamp(Instant.now())
                    .level(statusCode >= 500 ? LogLevel.ERROR : statusCode >= 400 ? LogLevel.WARN : LogLevel.INFO)
                    .logger(this.getClass().getName())
                    .thread(Thread.currentThread().getName())
                    .message(String.format("%s %s - %d", method, path, statusCode))
                    .requestId(requestId)
                    .userId(userId)
                    .method(method)
                    .path(path)
                    .statusCode(statusCode)
                    .duration(duration)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            applicationLogService.save(applicationLog);

        } catch (Exception e) {
            log.error("Failed to log request/response", e);
        }
    }
}
