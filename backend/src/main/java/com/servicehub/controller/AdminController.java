package com.servicehub.controller;

import com.servicehub.dto.CreateDepartmentRequest;
import com.servicehub.dto.UserResponse;
import com.servicehub.model.Department;
import com.servicehub.model.enums.Role;
import com.servicehub.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin", description = "Admin-only endpoints for user role management and department configuration")
public class AdminController {

    private final AdminService adminService;

    //user management

    @Operation(
            summary     = "Get all users",
            description = "Returns a list of all registered users in the system with their roles and departments."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of users retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin role required")
    })
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @Operation(
            summary     = "Get user by ID",
            description = "Retrieve a specific user's details by their unique ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin role required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @Operation(
            summary     = "Get users by role",
            description = "Filter and return users by their assigned role (ADMIN, AGENT, or USER)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered list of users returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role value"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin role required")
    })
    @GetMapping("/users/by-role")
    public ResponseEntity<List<UserResponse>> getUsersByRole(
            @Parameter(description = "Role to filter by (ADMIN, AGENT, USER)", example = "AGENT")
            @RequestParam Role role) {
        return ResponseEntity.ok(adminService.getUsersByRole(role));
    }

    @Operation(
            summary     = "Update user role",
            description = "Promote or demote a user's role. " +
                    "When promoting to AGENT, a departmentId is required. " +
                    "Cannot change the role of an ADMIN account."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User role updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role or missing departmentId for AGENT role"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin role required or attempted to modify another Admin"),
            @ApiResponse(responseCode = "404", description = "User or department not found")
    })
    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @Parameter(description = "User ID to update", example = "3")
            @PathVariable Long id,
            @Parameter(description = "New role to assign", example = "AGENT")
            @RequestParam Role role,
            @Parameter(description = "Department ID (required when assigning AGENT role)", example = "1")
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(adminService.updateUserRole(id, role, departmentId));
    }

    @Operation(
            summary     = "Delete a user",
            description = "Permanently delete a user account. Cannot delete an ADMIN account."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin role required or attempted to delete an Admin"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(
            @Parameter(description = "User ID to delete", example = "3")
            @PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

   //department management

    @Operation(
            summary     = "Get all departments",
            description = "Returns all departments including inactive ones. Use /api/departments for public active-only list."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of all departments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin role required")
    })
    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(adminService.getAllDepartments());
    }

    @Operation(
            summary     = "Get department by ID",
            description = "Retrieve a specific department's details by its unique ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin role required"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @GetMapping("/departments/{id}")
    public ResponseEntity<Department> getDepartmentById(
            @Parameter(description = "Department ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(adminService.getDepartmentById(id));
    }

    @Operation(
            summary     = "Create a new department",
            description = "Create a new service department with a name, category, and contact email. " +
                    "Department names must be unique."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Department created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or duplicate department name"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin role required")
    })
    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.status(201).body(adminService.createDepartment(request));
    }

    @Operation(
            summary     = "Update a department",
            description = "Update an existing department's name, category, or contact email."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin role required"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @PutMapping("/departments/{id}")
    public ResponseEntity<Department> updateDepartment(
            @Parameter(description = "Department ID to update", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.ok(adminService.updateDepartment(id, request));
    }

    @Operation(
            summary     = "Toggle department active status",
            description = "Activate or deactivate a department. " +
                    "Inactive departments are hidden from the registration dropdown."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department status toggled successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin role required"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @PutMapping("/departments/{id}/toggle")
    public ResponseEntity<Department> toggleDepartment(
            @Parameter(description = "Department ID to toggle", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleDepartmentActive(id));
    }

    @Operation(
            summary     = "Delete a department",
            description = "Permanently delete a department. " +
                    "Consider deactivating instead to preserve historical ticket data."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin role required"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @DeleteMapping("/departments/{id}")
    public ResponseEntity<String> deleteDepartment(
            @Parameter(description = "Department ID to delete", example = "1")
            @PathVariable Long id) {
        adminService.deleteDepartment(id);
        return ResponseEntity.ok("Department deleted successfully");
    }
}