package com.autumnus.notificationservice.common.api;

import com.autumnus.notificationservice.common.context.RequestContextHolder;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final Instant timestamp;
    private final String traceId;
    private final T data;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now())
                .traceId(RequestContextHolder.getContext().getTraceId())
                .data(data)
                .build();
    }
}
