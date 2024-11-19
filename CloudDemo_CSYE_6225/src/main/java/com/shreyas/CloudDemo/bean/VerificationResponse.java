package com.shreyas.CloudDemo.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerificationResponse {
    private boolean isSuccess;
    private String message;
}
