package com.shreyas.CloudDemo.exceptions;

import com.shreyas.CloudDemo.controller.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

import java.sql.SQLException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends BaseController {

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Void> handleSQLException(SQLException ex) {
        log.error("SQLException {}",ex.getMessage());
        return ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Void> handleIllegalArgumentException(AuthenticationException ex) {
        log.error("AuthenticationException {}",ex.getMessage());
        return ErrorResponse(HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentialException(AuthenticationException ex) {
        log.error("BadCredentialsException {}",ex.getMessage());
        return ErrorResponse(HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(HttpClientErrorException.Forbidden.class)
    public ResponseEntity<Void> handleUnauthorizedException(HttpClientErrorException.Unauthorized ex) {
        log.error("Unauthorized Exception {}",ex.getMessage());
        return ErrorResponse(HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Void> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        log.error("UsernameNotFoundException {}",ex.getMessage());
        return ErrorResponse(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleException(Exception ex) {
        log.error("Unexpected Exception Occurred {}",ex.getMessage());
        return ExceptionResponse(ex);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Void> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.error("HttpRequestMethodNotSupportedException {}",ex.getMessage());
        return ErrorResponse(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Void> handleBadRequestException(BadRequestException ex) {
        log.error("BadRequestException {}",ex.getMessage());
        return ErrorResponse(HttpStatus.BAD_REQUEST);
    }
}
