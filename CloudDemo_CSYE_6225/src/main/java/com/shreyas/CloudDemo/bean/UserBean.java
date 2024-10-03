package com.shreyas.CloudDemo.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserBean {
    private UUID id;

    @NotBlank(message = "First name must not be null")
    @Size(min = 2, max = 50, message = "First name must be between 2 to 50 characters")
    private String firstName;

    @NotBlank(message = "Last name must not be null")
    @Size(min = 2, max = 50, message = "Last name must be between 2 to 50 characters")
    private String lastName;

    @Size( min = 1, message = "Email can not be empty")
    @Email(message = "Invalid email address")
    private String email;

    @NotNull(message = "Password must not be null")
    @Size(min=8, message = "Password must be at least 8 characters long")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private LocalDateTime account_created;

    private LocalDateTime account_updated;
}
