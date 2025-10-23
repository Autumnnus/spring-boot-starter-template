package com.autumnus.spring_boot_starter_template.common.idempotency;

import com.autumnus.spring_boot_starter_template.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "idempotency_keys", indexes = {
        @Index(name = "uk_idempotency_key", columnList = "idempotencyKey", unique = true)
})
@Getter
@Setter
public class IdempotencyKey extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private String requestHash;

    @Column(columnDefinition = "TEXT")
    private String responseBody;

    private Integer statusCode;

    private Instant expiresAt;
}
