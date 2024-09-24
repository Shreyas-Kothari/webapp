package com.shreyas.CloudDemo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
@Slf4j
public abstract class BaseController {
    protected ResponseEntity<Void> SuccessResponse() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_LENGTH, "0");
        return ResponseEntity.ok()
                .headers(headers)
                .build();
    }

    protected <T> ResponseEntity<T> SuccessResponse(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(body.toString().length()));
        return ResponseEntity.ok()
                .headers(headers)
                .body(body);
    }

    protected ResponseEntity<Void> ErrorResponse(HttpStatus status) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_LENGTH, "0");
        return ResponseEntity.status(status)
                .headers(headers)
                .build();
    }

    protected <T> ResponseEntity<T> ErrorResponse(HttpStatus status, T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(body.toString().length()));
        return ResponseEntity.status(status)
                .headers(headers)
                .body(body);
    }

    protected <T> ResponseEntity<T> NoContentFoundResponse() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_LENGTH, "0");
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .headers(headers)
                .build();
    }

    protected <T> ResponseEntity<T> ExceptionResponse(Exception ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_LENGTH, "0");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .headers(headers)
                .build();
    }
}
