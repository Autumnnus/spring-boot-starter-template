package com.autumnus.notificationservice.common.context;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RequestContext {

    private final String traceId;
    private final String userId;

    public static RequestContext empty() {
        return RequestContext.builder().build();
    }
}
