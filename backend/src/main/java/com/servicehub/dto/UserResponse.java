package com.servicehub.dto;

import com.servicehub.model.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String department;
    private String departmentCategory;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .department(user.getDepartment() != null
                        ? user.getDepartment().getName()
                        : null)
                .departmentCategory(user.getDepartment() != null  // ← add this
                        ? user.getDepartment().getCategory().name()
                        : null)
                .build();
    }
}