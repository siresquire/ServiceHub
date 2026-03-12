package com.servicehub.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
public class ServiceRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;


    @NotNull
    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull
    @NotBlank(message = "Category is required")
    private String category;

    @NotNull
    @NotBlank(message = "Priority is required")
    private String priority;
    /**
     * Optional: when provided, department is set on the request
     * and category can be derived from it.
     */
    @Positive(message = "Department ID must be a positive number")
    @NotNull
    private Long departmentId;
}
