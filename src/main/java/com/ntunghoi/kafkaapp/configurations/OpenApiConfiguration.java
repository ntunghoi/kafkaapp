package com.ntunghoi.kafkaapp.configurations;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfiguration {
    public static final String ACTUATOR_TAG = "Actuator";
    public static final String AUTHENTICATION_TAG = "Authentication";
    public static final String ACCOUNTS_TAG = "Accounts";

    @Value("${api.version}")
    private String apiVersion;

    @Value("${api.server.url}")
    private String apiServerUrl;

    @Value("${api.server.description}")
    private String apiServerDescription;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Sample application")
                                .version(apiVersion)
                                .description("Sample description")
                                .contact(
                                        new Contact()
                                                .name("Admin")
                                                .email("admin@ntunghoi.com")
                                                .url("https://www.ntunghoi.com")
                                )
                                .termsOfService("https://www.ntunghoi.com/terms-of-service")
                                .license(
                                        new License()
                                                .name("API License")
                                                .url("https://www.ntunghoi.com/license")
                                )
                )
                .externalDocs(
                        new ExternalDocumentation()
                                .description("API External Documentation")
                                .url("https://www.ntunghoi.com/api-external-documentation")
                )
                .servers(
                        List.of(
                                new Server()
                                        .description(apiServerDescription)
                                        .url(apiServerUrl)
                        )
                )
                .tags(
                        List.of(
                                new Tag().name(ACTUATOR_TAG).description("Actuator endpoints"),
                                new Tag().name(AUTHENTICATION_TAG).description("Authentication functions"),
                                new Tag().name(ACCOUNTS_TAG).description("Accounts related functions")
                        )
                );
    }
}
