package com.autumnus.spring_boot_starter_template.common.logging.filter;

import com.autumnus.spring_boot_starter_template.common.context.RequestContext;
import com.autumnus.spring_boot_starter_template.common.context.RequestContextHolder;
import com.autumnus.spring_boot_starter_template.common.logging.annotation.NoLog;
import com.autumnus.spring_boot_starter_template.common.logging.dto.ApplicationLogDto;
import com.autumnus.spring_boot_starter_template.common.logging.service.ApplicationLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@NoLog
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_USER_ID = "userId";
    private static final String MDC_IP_ADDRESS = "ipAddress";
    private static final String MDC_USER_AGENT = "userAgent";

    private final ApplicationLogService applicationLogService;

    public LoggingFilter(ApplicationLogService applicationLogService) {
        this.applicationLogService = applicationLogService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final Instant start = Instant.now();
        final RequestContext context = RequestContextHolder.getContext();
        String requestId = context.getRequestId();
        if (!StringUtils.hasText(requestId)) {
            requestId = UUID.randomUUID().toString();
            context.setRequestId(requestId);
        }
        MDC.put(MDC_REQUEST_ID, requestId);

        final String ipAddress = Optional.ofNullable(context.getIpAddress())
                .filter(StringUtils::hasText)
                .orElseGet(request::getRemoteAddr);
        context.setIpAddress(ipAddress);
        MDC.put(MDC_IP_ADDRESS, ipAddress);

        final String userAgent = Optional.ofNullable(request.getHeader("User-Agent"))
                .filter(StringUtils::hasText)
                .orElse(null);
        context.setUserAgent(userAgent);
        if (StringUtils.hasText(userAgent)) {
            MDC.put(MDC_USER_AGENT, userAgent);
        }

        context.setHttpMethod(request.getMethod());
        context.setRequestUri(request.getRequestURI());
        context.setRequestTime(start);

        Throwable capturedException = null;
        try {
            filterChain.doFilter(request, response);
        } catch (Throwable ex) {
            capturedException = ex;
            throw ex;
        } finally {
            final Instant end = Instant.now();
            final long duration = Duration.between(start, end).toMillis();
            context.setDuration(duration);
            context.setResponseStatus(response.getStatus());

            final String userIdValue = context.getUserId();
            if (StringUtils.hasText(userIdValue)) {
                MDC.put(MDC_USER_ID, userIdValue);
            }

            final String message = capturedException == null
                    ? "Request completed"
                    : "Request failed";
            if (capturedException == null) {
                log.info("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
            } else {
                log.error("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), response.getStatus(), duration, capturedException);
            }

            final ApplicationLogDto.ExceptionInfo exceptionInfo = capturedException == null
                    ? null
                    : ApplicationLogDto.ExceptionInfo.builder()
                    .className(capturedException.getClass().getName())
                    .message(capturedException.getMessage())
                    .stackTrace(buildStackTrace(capturedException))
                    .build();

            final ApplicationLogDto dto = ApplicationLogDto.builder()
                    .timestamp(start)
                    .level(capturedException == null ? "INFO" : "ERROR")
                    .logger(LoggingFilter.class.getName())
                    .thread(Thread.currentThread().getName())
                    .message(message)
                    .requestId(requestId)
                    .userId(parseLong(userIdValue))
                    .method(request.getMethod())
                    .path(request.getRequestURI())
                    .statusCode(response.getStatus())
                    .duration(duration)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .exception(exceptionInfo)
                    .build();
            applicationLogService.record(dto);

            MDC.remove(MDC_REQUEST_ID);
            MDC.remove(MDC_USER_ID);
            MDC.remove(MDC_IP_ADDRESS);
            MDC.remove(MDC_USER_AGENT);
        }
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
