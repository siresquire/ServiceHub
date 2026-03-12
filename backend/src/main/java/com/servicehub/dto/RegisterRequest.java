package com.servicehub.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 128, message = "Password must be between 6 and 128 characters")
    private String password;

    @NotNull(message = "Department is required")
    @Positive(message = "Department ID must be a positive number")
    private Long departmentId;
}
