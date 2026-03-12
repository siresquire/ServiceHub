package com.servicehub.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
public class ServiceRequestDto {


    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;



    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;


    @NotBlank(message = "Priority is required")
    private String priority;
    /**
     * Optional: when provided, department is set on the request
     * and category can be derived from it.
     */
    @Positive(message = "Department ID must be a positive number")
    private Long departmentId;
}
