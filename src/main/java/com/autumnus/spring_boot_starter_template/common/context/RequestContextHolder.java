package com.autumnus.spring_boot_starter_template.common.context;

public final class RequestContextHolder {

    private static final ThreadLocal<RequestContext> CONTEXT =
            ThreadLocal.withInitial(RequestContext::empty);

    private RequestContextHolder() {
    }

    public static RequestContext getContext() {
        return CONTEXT.get();
    }

    public static void setContext(RequestContext context) {
        CONTEXT.set(context);
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
