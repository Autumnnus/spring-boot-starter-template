package com.autumnus.notificationservice.common.context.filter;

import com.autumnus.notificationservice.common.context.RequestContext;
import com.autumnus.notificationservice.common.context.RequestContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestContextEnrichmentFilter implements jakarta.servlet.Filter {

    private static final String TRACE_HEADER = "X-Trace-Id";
    private static final String USER_HEADER = "X-User-Id";

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        final String traceId = Optional.ofNullable(req.getHeader(TRACE_HEADER))
                .filter(s -> !s.isBlank())
                .orElse(UUID.randomUUID().toString());
        final String userId = req.getHeader(USER_HEADER);
        final RequestContext context = RequestContext.builder()
                .traceId(traceId)
                .userId(userId)
                .build();
        RequestContextHolder.setContext(context);
        try {
            res.setHeader(TRACE_HEADER, traceId);
            chain.doFilter(request, response);
        } finally {
            RequestContextHolder.clear();
        }
    }

}
