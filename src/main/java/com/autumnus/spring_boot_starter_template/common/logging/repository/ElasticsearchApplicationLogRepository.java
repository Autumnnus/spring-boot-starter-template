package com.autumnus.spring_boot_starter_template.common.logging.repository;

import com.autumnus.spring_boot_starter_template.common.logging.config.LoggingProperties;
import com.autumnus.spring_boot_starter_template.common.logging.document.ApplicationLog;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public class ElasticsearchApplicationLogRepository implements ApplicationLogRepository {

    private final ElasticsearchOperations elasticsearchOperations;
    private final LoggingProperties loggingProperties;

    public ElasticsearchApplicationLogRepository(
            ElasticsearchOperations elasticsearchOperations,
            LoggingProperties loggingProperties
    ) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.loggingProperties = loggingProperties;
    }

    @Override
    public void save(ApplicationLog log) {
        if (log.getTimestamp() == null) {
            log.setTimestamp(Instant.now());
        }
        final String indexName = loggingProperties.getApplication().getElasticsearch()
                .resolveIndexName(log.getTimestamp());
        elasticsearchOperations.save(log, IndexCoordinates.of(indexName));
    }
}
