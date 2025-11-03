package com.autumnus.spring_boot_starter_template.common.logging.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for logging
 */
@Configuration
@ConfigurationProperties(prefix = "logging")
@Data
public class LoggingProperties {

    private AuditConfig audit = new AuditConfig();
    private ApplicationConfig application = new ApplicationConfig();

    @Data
    public static class AuditConfig {
        private boolean enabled = true;
        private boolean async = true;
        private int retentionDays = 365;
    }

    @Data
    public static class ApplicationConfig {
        private boolean enabled = true;
        private ElasticsearchConfig elasticsearch = new ElasticsearchConfig();
    }

    @Data
    public static class ElasticsearchConfig {
        private String indexPattern = "application-logs-{yyyy.MM}";
        private int retentionDays = 90;
    }
}
