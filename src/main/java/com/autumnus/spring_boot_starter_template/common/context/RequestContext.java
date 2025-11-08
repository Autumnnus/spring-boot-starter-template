package com.autumnus.spring_boot_starter_template.common.context;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class RequestContext {

    private String traceId;
    private String requestId;
    private String ipAddress;
    private String userId;
    private String clientId;
    private String userAgent;
    private String httpMethod;
    private String requestUri;
    private Integer responseStatus;
    private Long duration;

    private Instant requestTime;

    public static RequestContext empty() {
        return RequestContext.builder().build();
    }
}
