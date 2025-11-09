package com.autumnus.spring_boot_starter_template.common.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseDto {

    private Long id;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
}
