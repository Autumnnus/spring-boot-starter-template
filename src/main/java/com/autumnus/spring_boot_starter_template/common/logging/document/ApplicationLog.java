package com.autumnus.spring_boot_starter_template.common.logging.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Document(indexName = "application-logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationLog {

    @Id
    private String id;

    @Field(type = FieldType.Date)
    private Instant timestamp;

    @Field(type = FieldType.Keyword)
    private String level;

    @Field(type = FieldType.Keyword)
    private String logger;

    @Field(type = FieldType.Keyword)
    private String thread;

    @Field(type = FieldType.Text)
    private String message;

    @Field(type = FieldType.Keyword, name = "requestId")
    private String requestId;

    @Field(type = FieldType.Long, name = "userId")
    private Long userId;

    @Field(type = FieldType.Keyword)
    private String method;

    @Field(type = FieldType.Keyword)
    private String path;

    @Field(type = FieldType.Integer, name = "statusCode")
    private Integer statusCode;

    @Field(type = FieldType.Long)
    private Long duration;

    @Field(type = FieldType.Ip, name = "ipAddress")
    private String ipAddress;

    @Field(type = FieldType.Text, name = "userAgent")
    private String userAgent;

    @Field(type = FieldType.Object)
    private ExceptionDocument exception;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExceptionDocument {
        @Field(type = FieldType.Keyword, name = "class")
        private String className;

        @Field(type = FieldType.Text)
        private String message;

        @Field(type = FieldType.Text, name = "stackTrace")
        private String stackTrace;
    }
}
