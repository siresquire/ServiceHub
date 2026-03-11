package com.servicehub.dto;

import com.servicehub.model.enums.RequestCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDepartmentRequest {

    @NotBlank(message = "Department name is required")
    private String name;

    @NotNull(message = "Category is required")
    private RequestCategory category;

    @Email(message = "Invalid email format")
    private String contactEmail;
}
