package com.shreyas.CloudDemo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
@Slf4j
public abstract class BaseController {
    protected ResponseEntity<Void> SuccessResponse() {
        return ResponseEntity.ok().build();
    }

    protected <T> ResponseEntity<T> SuccessResponse(T body) {
        log.info("Success response !!");
        return ResponseEntity.ok().body(body);
    }

    protected <T> ResponseEntity<T> CreatedResponse(T body) {
        log.info("Created response successfully !!");
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    protected ResponseEntity<Void> ErrorResponse(HttpStatus status) {
        log.error("Error response with status {}", status);
        return ResponseEntity.status(status).build();
    }

    protected <T> ResponseEntity<T> ErrorResponse(HttpStatus status, T body) {
        log.error("Error response with status: {} and body: {}", status, body);
        return ResponseEntity.status(status).body(body);
    }

    protected <T> ResponseEntity<T> NoContentResponse() {
        log.info("No content response returned!!");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    protected <T> ResponseEntity<T> ExceptionResponse(Exception ex) {
        log.error("Unexpected Exception occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
