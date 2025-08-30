package com.innoverse.erp_edu_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ZW Schools ERP API")
                        .version("1.0")
                        .description("API documentation for Schools - Tenant header required for all school operations"));
    }

    @Bean
    public GroupedOpenApi platformGroup() {
        return GroupedOpenApi.builder()
                .group("platform")
                .pathsToMatch("/api/platform/**")
                .build();
    }

    @Bean
    public GroupedOpenApi tenantGroup() {
        return GroupedOpenApi.builder()
                .group("schools")
                .pathsToMatch("/api/schools/**")
                .build();
    }

    @Bean
    public GlobalOpenApiCustomizer globalHeaderCustomizer() {
        return openApi -> {
            // Add tenant header to all operations under /api/schools/**
            openApi.getPaths().forEach((path, pathItem) -> {
                if (path.startsWith("/api/schools/")) {
                    pathItem.readOperations().forEach(operation -> {
                        Parameter tenantHeader = new Parameter()
                                .in("header")
                                .name("X-Tenant-ID")
                                .description("Tenant/School identifier. Required for routing to tenant-specific schemas. " +
                                        "Can be School UUID or schema name.")
                                .required(true)
                                .example("123e4567-e89b-12d3-a456-426614174000");

                        operation.addParametersItem(tenantHeader);
                    });
                }
            });
        };
    }
}