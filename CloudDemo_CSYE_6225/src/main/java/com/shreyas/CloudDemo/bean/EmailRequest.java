package com.shreyas.CloudDemo.bean;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class EmailRequest {
    @NotNull(message = "The email recipient can not be null")
    private String recipient;
    @NotNull(message = "The email subject can not be null")
    private String subject;
    @NotNull(message = "The email message can not be null")
    private String message;
}