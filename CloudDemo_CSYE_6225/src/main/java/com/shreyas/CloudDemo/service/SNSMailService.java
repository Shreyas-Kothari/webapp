package com.shreyas.CloudDemo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreyas.CloudDemo.bean.EmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class SNSMailService {
    private final ObjectMapper objectMapper;
    private final SnsClient snsClient;
    @Value("${aws.sns.topic.arn}")
    private String topicArn;

    public boolean publishMailRequestToTopic(EmailRequest emailRequest) throws JsonProcessingException {
        log.info("Adding mail request to SNS topic");

        String messageJson = objectMapper.writeValueAsString(emailRequest);

        PublishRequest publishRequest = PublishRequest.builder()
                .topicArn(topicArn)
                .message(messageJson)
                .build();

        PublishResponse response = snsClient.publish(publishRequest);
        log.info("MessageID for publishing to SNS topic is {} with status code {}", response.messageId(), response.sdkHttpResponse().statusText().toString());
        return response.sdkHttpResponse().isSuccessful();
    }
}
