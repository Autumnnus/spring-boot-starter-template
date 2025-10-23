package com.autumnus.spring_boot_starter_template.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private final ErrorDetail error;

    @Getter
    @Builder
    public static class ErrorDetail {
        private final String code;
        private final String message;
        private final String traceId;
        private final Instant timestamp;
    }

    public static ApiError of(String code, String message, String traceId) {
        return ApiError.builder()
                .error(ErrorDetail.builder()
                        .code(code)
                        .message(message)
                        .traceId(traceId)
                        .timestamp(Instant.now())
                        .build())
                .build();
    }
}
