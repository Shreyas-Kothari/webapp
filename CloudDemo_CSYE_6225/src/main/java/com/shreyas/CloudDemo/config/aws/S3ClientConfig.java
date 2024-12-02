package com.shreyas.CloudDemo.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;

@Configuration
public class S3ClientConfig {
    @Value("${aws.s3.region}")
    private String s3Region;

    @Primary
    @Bean
    public S3Client s3client() {
        return S3Client.builder()
                .region(Region.of(s3Region))
                // comment below line to run in local
//                .credentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();
    }
}
