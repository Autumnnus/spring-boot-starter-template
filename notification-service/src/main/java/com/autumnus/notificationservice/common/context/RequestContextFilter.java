package com.autumnus.notificationservice.common.context;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class RequestContextFilter extends OncePerRequestFilter {

    public static final String TRACE_HEADER = "X-Trace-Id";
    public static final String USER_HEADER = "X-User-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String traceId = Optional.ofNullable(request.getHeader(TRACE_HEADER))
                .filter(value -> !value.isBlank())
                .orElse(UUID.randomUUID().toString());
        final String userId = request.getHeader(USER_HEADER);
        final RequestContext context = RequestContext.builder()
                .traceId(traceId)
                .userId(userId)
                .build();
        RequestContextHolder.setContext(context);
        try {
            response.setHeader(TRACE_HEADER, traceId);
            filterChain.doFilter(request, response);
        } finally {
            RequestContextHolder.clear();
        }
    }
}
