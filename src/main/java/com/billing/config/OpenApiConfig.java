package com.billing.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI billingOpenApi() {

        return new OpenAPI()

                .info(new Info()
                        .title("Usage Based Billing Service")
                        .description("""
                                REST APIs for recording usage events,
                                applying configurable pricing strategies,
                                and generating invoices.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Mohan Sai Pujari")
                                .email("your-email@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))

                .externalDocs(
                        new ExternalDocumentation()
                                .description("Project Documentation")
                                .url("https://github.com/")
                );
    }
}