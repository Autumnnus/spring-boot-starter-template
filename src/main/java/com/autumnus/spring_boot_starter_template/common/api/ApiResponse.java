package com.autumnus.spring_boot_starter_template.common.api;

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
    private final PaginationMeta pagination;

    public static <T> ApiResponse<T> ok(String traceId, T data) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now())
                .traceId(traceId)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(String traceId, T data, PaginationMeta pagination) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now())
                .traceId(traceId)
                .data(data)
                .pagination(pagination)
                .build();
    }
}
