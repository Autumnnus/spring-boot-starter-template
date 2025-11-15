package com.autumnus.notificationservice.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final Instant timestamp;
    private final String traceId;
    private final String code;
    private final String message;
}
