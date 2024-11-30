package com.shreyas.CloudDemo.config.aws;

import com.shreyas.CloudDemo.service.SecretsManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSourceConfig {
    private final SecretsManagerService secretsManagerService;
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${SPRING_DATASOURCE_USERNAME:#{null}}") // Default to null if not set
    private String dbUsername;

    @Value("${SPRING_DATASOURCE_PASSWORD:#{null}}") // Default to null if not set
    private String dbPassword;

    @Value("${aws.secrets.name}")
    private String secretName;

    @Bean
    @Primary
    public DataSource dataSource() {
        if (dbUsername != null && dbPassword != null) {
            // Use credentials from application.properties or environment variables
            log.info("Using database credentials from application properties i.e environment variables.");
        } else {
            log.info("Fetching database credentials from AWS Secrets Manager.");
            Map<String, String> secrets = secretsManagerService.getSecret(secretName);

            dbUsername = secrets.get("username");
            dbPassword = secrets.get("password");

            System.out.println("The username and password are: " + dbUsername + " and " + dbPassword);

            if (dbUsername == null || dbPassword == null) {
                throw new IllegalStateException("Database credentials not found in AWS Secrets Manager");
            }
        }

        return DataSourceBuilder
                .create()
                .url(dbUrl)
                .username(dbUsername)
                .password(dbPassword)
                .build();
    }
}
