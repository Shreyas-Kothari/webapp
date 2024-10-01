package com.shreyas.CloudDemo.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Email must not be null")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Password must not be null")
    @Size(min=8, message = "Password must be at least 8 characters long")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime account_created;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime account_updated;
}
