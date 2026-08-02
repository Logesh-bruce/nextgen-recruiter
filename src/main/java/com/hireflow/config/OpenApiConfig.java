package com.hireflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 3.0 configuration.
 * Accessible at: {@code /swagger-ui.html} and {@code /v3/api-docs}
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI hireFlowOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("HireFlow AI — Backend API")
                        .description("REST API for HireFlow AI: an AI-powered SaaS recruitment platform. " +
                                "Covers job management, application tracking, resume parsing, AI match scoring, " +
                                "interview scheduling, and notifications.")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("HireFlow Team")
                                .email("dev@hireflow.ai"))
                        .license(new License().name("Private")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT access token obtained from /api/v1/auth/login")));
    }
}
