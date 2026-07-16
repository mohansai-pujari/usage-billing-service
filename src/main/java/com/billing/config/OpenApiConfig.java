package com.billing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Usage Based Billing Service")
                        .description("Record usage, apply pricing rules, and generate invoices.")
                        .version("1.0.0"));
    }

    @Bean
    @Conditional(BulkUploadEnvironmentCondition.class)
    OpenApiCustomizer internalTestingOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getInfo() != null) {
                openApi.getInfo().setDescription(
                        openApi.getInfo().getDescription()
                                + """

                                See **Internal Testing** for Swagger-only bulk ingest helpers.""");
            }
            openApi.addTagsItem(new Tag()
                    .name("Internal Testing")
                    .description("Local/manual test helpers exposed in Swagger UI only"));
        };
    }
}
