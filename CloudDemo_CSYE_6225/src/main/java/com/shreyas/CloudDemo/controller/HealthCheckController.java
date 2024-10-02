package com.shreyas.CloudDemo.controller;

import com.shreyas.CloudDemo.service.interfaces.HealthCheckService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/healthz", consumes = MediaType.ALL_VALUE)
public class HealthCheckController extends BaseController {

    @Autowired
    private final HealthCheckService healthCheckService;

    @GetMapping
    @Operation(summary = "Get the health check status",
            description = "The GET application gives the health check status for the database and other APIs services"
    )
    public ResponseEntity<Void> checkHealth(@RequestBody(required = false) Object o) throws BadRequestException {
        if(o!=null)
            throw new BadRequestException("Request Body not allowed");

        try {
            if (healthCheckService.isDataConnectionAvailable())
                return SuccessResponse();
            else
                return ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE);
        } catch (SQLException e) {
            return ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            return ExceptionResponse(e);
        }

    }
}
