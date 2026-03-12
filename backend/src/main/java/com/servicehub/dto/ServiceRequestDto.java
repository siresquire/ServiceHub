package com.servicehub.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
public class ServiceRequestDto {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private String category;
    @NotNull
    private String priority;
    /** Optional: when provided, department is set on the request and category can be derived from it */
    private Long departmentId;
}
