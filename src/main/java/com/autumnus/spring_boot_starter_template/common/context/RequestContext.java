package com.autumnus.spring_boot_starter_template.common.context;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RequestContext {

    private String traceId;
    private String ipAddress;
    private String userId;
    private String clientId;

    public static RequestContext empty() {
        return RequestContext.builder().build();
    }
}
