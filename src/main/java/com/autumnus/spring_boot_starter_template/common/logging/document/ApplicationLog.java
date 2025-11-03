package com.autumnus.spring_boot_starter_template.common.logging.document;

import com.autumnus.spring_boot_starter_template.common.logging.enums.LogLevel;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.Map;

/**
 * Document representing an application log entry stored in Elasticsearch
 */
@Document(indexName = "application-logs-#{T(java.time.LocalDate).now().toString().replace('-', '.')}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationLog {

    @Id
    private String id;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant timestamp;

    @Field(type = FieldType.Keyword)
    private LogLevel level;

    @Field(type = FieldType.Keyword)
    private String logger;

    @Field(type = FieldType.Keyword)
    private String thread;

    @Field(type = FieldType.Text)
    private String message;

    @Field(type = FieldType.Keyword)
    private String requestId;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Keyword)
    private String method;

    @Field(type = FieldType.Keyword)
    private String path;

    @Field(type = FieldType.Integer)
    private Integer statusCode;

    @Field(type = FieldType.Long)
    private Long duration;

    @Field(type = FieldType.Ip)
    private String ipAddress;

    @Field(type = FieldType.Text)
    private String userAgent;

    @Field(type = FieldType.Object)
    private ExceptionInfo exception;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExceptionInfo {
        private String className;
        private String message;
        private String stackTrace;
    }
}
