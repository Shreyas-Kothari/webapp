package com.shreyas.CloudDemo.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class SNSClientConfig {
    @Value("${aws.s3.region}")
    private String s3Region;

    @Primary
    @Bean
    public SnsClient snsclient() {
        return SnsClient.builder()
                .region(Region.of(s3Region))
                // comment below line to run in local
//                .credentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();
    }
}
