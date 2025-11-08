package com.autumnus.spring_boot_starter_template.common.logging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@ConfigurationProperties(prefix = "logging")
public class LoggingProperties {

    private final Audit audit = new Audit();
    private final Application application = new Application();

    public Audit getAudit() {
        return audit;
    }

    public Application getApplication() {
        return application;
    }

    public static class Audit {

        private boolean enabled = true;
        private boolean async = true;
        private int retentionDays = 365;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isAsync() {
            return async;
        }

        public void setAsync(boolean async) {
            this.async = async;
        }

        public int getRetentionDays() {
            return retentionDays;
        }

        public void setRetentionDays(int retentionDays) {
            this.retentionDays = retentionDays;
        }
    }

    public static class Application {

        private boolean enabled = true;
        private boolean async = true;
        private final Elasticsearch elasticsearch = new Elasticsearch();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isAsync() {
            return async;
        }

        public void setAsync(boolean async) {
            this.async = async;
        }

        public Elasticsearch getElasticsearch() {
            return elasticsearch;
        }
    }

    public static class Elasticsearch {

        private String indexPattern = "application-logs";
        private int retentionDays = 90;

        public String getIndexPattern() {
            return indexPattern;
        }

        public void setIndexPattern(String indexPattern) {
            this.indexPattern = indexPattern;
        }

        public int getRetentionDays() {
            return retentionDays;
        }

        public void setRetentionDays(int retentionDays) {
            this.retentionDays = retentionDays;
        }

        public String resolveIndexName(Instant timestamp) {
            final Instant effectiveTimestamp = timestamp != null ? timestamp : Instant.now();
            if (!StringUtils.hasText(indexPattern)) {
                return "application-logs";
            }
            final int start = indexPattern.indexOf('{');
            final int end = indexPattern.indexOf('}', start + 1);
            if (start >= 0 && end > start) {
                final String datePattern = indexPattern.substring(start + 1, end);
                final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datePattern)
                        .withZone(ZoneOffset.UTC);
                return indexPattern.substring(0, start)
                        + formatter.format(effectiveTimestamp)
                        + indexPattern.substring(end + 1);
            }
            return indexPattern;
        }
    }
}
