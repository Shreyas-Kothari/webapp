package com.shreyas.CloudDemo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SecretsManagerService {
    private final SecretsManagerClient secretsManagerClient;
    private final ObjectMapper objectMapper;

    public Map<String, String> getSecret(String secretName) {
        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
        try {
            return objectMapper.readValue(response.secretString(), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse secret", e);
        }
    }
}
