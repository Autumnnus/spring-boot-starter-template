package com.autumnus.notificationservice.common.context;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RequestContext {

    private String traceId;
    private String userId;

    public static RequestContext empty() {
        return RequestContext.builder().build();
    }
}
