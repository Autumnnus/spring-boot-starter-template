package com.autumnus.spring_boot_starter_template.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Boot Starter Template")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))
                        .addParameters("IdempotencyKeyHeader",
                                new io.swagger.v3.oas.models.parameters.Parameter()
                                        .in("header")
                                        .name("Idempotency-Key")
                                        .required(false)
                                        .description("Idempotency Key")
                                        .schema(new io.swagger.v3.oas.models.media.StringSchema())
                        )
                );
    }

    @Bean
    public OperationCustomizer customize() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(new io.swagger.v3.oas.models.parameters.Parameter().$ref("#/components/parameters/IdempotencyKeyHeader"));
            return operation;
        };
    }
}
