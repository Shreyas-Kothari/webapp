package com.shreyas.CloudDemo.controller;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatusCode;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class APIResponse<T> {
    private int statusCode;
    private String status;
    private T data;
    private String message;

    public APIResponse(String status, T data, String message, HttpStatusCode code) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.statusCode = code.value();
    }

    @Setter
    @Getter
    public static class ErrorDetail {
        private String code;
        private String message;

        public ErrorDetail(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
