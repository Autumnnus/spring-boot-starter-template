package com.autumnus.spring_boot_starter_template.common.api;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaginationMeta {

    private final long page;
    private final long size;
    private final long totalElements;
    private final long totalPages;
}
