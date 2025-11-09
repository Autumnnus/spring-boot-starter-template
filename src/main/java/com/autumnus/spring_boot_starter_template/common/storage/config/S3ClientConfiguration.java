package com.autumnus.spring_boot_starter_template.common.storage.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3ClientConfiguration {

    @Bean
    public S3Client s3Client(S3Properties properties) {
        final S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(properties.getRegion()));

        if (properties.getEndpoint() != null) {
            builder.endpointOverride(properties.getEndpoint());
        }

        builder.serviceConfiguration(software.amazon.awssdk.services.s3.S3Configuration.builder()
                .pathStyleAccessEnabled(properties.isPathStyleAccess())
                .build());

        builder.credentialsProvider(resolveCredentialsProvider(properties));

        return builder.build();
    }

    private AwsCredentialsProvider resolveCredentialsProvider(S3Properties properties) {
        if (properties.getAccessKey() != null && properties.getSecretKey() != null) {
            final AwsBasicCredentials credentials = AwsBasicCredentials.create(
                    properties.getAccessKey(),
                    properties.getSecretKey()
            );
            return StaticCredentialsProvider.create(credentials);
        }
        return DefaultCredentialsProvider.create();
    }
}
