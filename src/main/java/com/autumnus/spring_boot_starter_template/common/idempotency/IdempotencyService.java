package com.autumnus.spring_boot_starter_template.common.idempotency;

import java.time.Instant;
import java.util.Optional;

public interface IdempotencyService {

    Optional<IdempotencyKey> findByKey(String key);

    IdempotencyKey save(String key, String requestHash, String responseBody, int statusCode, Instant expiresAt);
}
