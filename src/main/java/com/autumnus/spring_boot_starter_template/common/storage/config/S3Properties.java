package com.autumnus.spring_boot_starter_template.common.storage.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "application.storage.s3")
public class S3Properties {

    @NotBlank
    private String bucket;

    @NotBlank
    private String region;

    private String accessKey;

    private String secretKey;

    private URI endpoint;

    private boolean pathStyleAccess = false;

    /**
     * Optional base URL for publicly accessible assets. Example: https://cdn.example.com
     */
    private String publicBaseUrl;
}
