package com.autumnus.spring_boot_starter_template.common.idempotency;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class IdempotencyServiceImpl implements IdempotencyService {

    private final IdempotencyKeyRepository repository;

    public IdempotencyServiceImpl(IdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IdempotencyKey> findByKey(String key) {
        return repository.findByIdempotencyKey(key)
                .filter(record -> record.getExpiresAt() == null || record.getExpiresAt().isAfter(Instant.now()));
    }

    @Override
    @Transactional
    public IdempotencyKey save(String key, String requestHash, String responseBody, int statusCode, Instant expiresAt) {
        final IdempotencyKey entity = new IdempotencyKey();
        entity.setIdempotencyKey(key);
        entity.setRequestHash(requestHash);
        entity.setResponseBody(responseBody);
        entity.setStatusCode(statusCode);
        entity.setExpiresAt(expiresAt);
        return repository.save(entity);
    }
}
