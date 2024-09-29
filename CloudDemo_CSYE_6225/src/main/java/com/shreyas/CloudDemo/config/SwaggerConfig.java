package com.shreyas.CloudDemo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@OpenAPIDefinition(info = @Info(
        title = "Task Services",
        version = "1.0",
        description = "API documentation for Spring Boot Application of CloudDemo project",
        contact = @io.swagger.v3.oas.annotations.info.Contact(name = "Shreyas Kothari", email = "kothari.shr@northeastern.edu"),
        license = @io.swagger.v3.oas.annotations.info.License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.html")
))
public class SwaggerConfig {

    private final ControllerNameCustomizer controllerNameCustomizer;

    @Bean
    public GroupedOpenApi customApi() {
        return GroupedOpenApi.builder()
                .group("cloud-demo-api")
                .packagesToScan("com.shreyas.CloudDemo.controller") // Specify your controllers' package
                .addOperationCustomizer(controllerNameCustomizer)
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("basicScheme",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")))
                .addSecurityItem(new SecurityRequirement().addList("basicScheme"));
    }
}