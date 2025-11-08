package com.autumnus.spring_boot_starter_template.common.logging;

import com.autumnus.spring_boot_starter_template.common.context.RequestContext;
import com.autumnus.spring_boot_starter_template.common.context.RequestContextHolder;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter implements Filter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TRACE_ID_MDC_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            final String traceId = Optional.ofNullable(httpRequest.getHeader(TRACE_ID_HEADER))
                    .filter(header -> !header.isBlank())
                    .orElse(UUID.randomUUID().toString());
            MDC.put(TRACE_ID_MDC_KEY, traceId);
            final RequestContext context = RequestContext.builder()
                    .traceId(traceId)
                    .ipAddress(httpRequest.getRemoteAddr())
                    .requestTime(Instant.now())
                    .build();
            RequestContextHolder.setContext(context);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_MDC_KEY);
            RequestContextHolder.clear();
        }
    }
}
