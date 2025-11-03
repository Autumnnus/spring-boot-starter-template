package com.autumnus.spring_boot_starter_template.common.logging.repository;

import com.autumnus.spring_boot_starter_template.common.logging.document.ApplicationLog;
import com.autumnus.spring_boot_starter_template.common.logging.enums.LogLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for ApplicationLog document in Elasticsearch
 */
@Repository
public interface ApplicationLogRepository extends ElasticsearchRepository<ApplicationLog, String> {

    /**
     * Find logs by request ID
     */
    List<ApplicationLog> findByRequestId(String requestId);

    /**
     * Find logs by user ID
     */
    Page<ApplicationLog> findByUserId(Long userId, Pageable pageable);

    /**
     * Find logs by level
     */
    Page<ApplicationLog> findByLevel(LogLevel level, Pageable pageable);

    /**
     * Find logs by level and time range
     */
    Page<ApplicationLog> findByLevelAndTimestampBetween(LogLevel level, Instant start, Instant end, Pageable pageable);

    /**
     * Find logs by path
     */
    Page<ApplicationLog> findByPath(String path, Pageable pageable);

    /**
     * Find error logs
     */
    Page<ApplicationLog> findByLevelIn(List<LogLevel> levels, Pageable pageable);
}
