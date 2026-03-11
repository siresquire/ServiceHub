package com.servicehub.controller;

import com.servicehub.dto.CreateDepartmentRequest;
import com.servicehub.dto.UserResponse;
import com.servicehub.model.Department;
import com.servicehub.model.enums.Role;
import com.servicehub.service.AdminService;
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
public class AdminController {

    private final AdminService adminService;



    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @GetMapping("/users/by-role")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@RequestParam Role role) {
        return ResponseEntity.ok(adminService.getUsersByRole(role));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long id,
            @RequestParam Role role,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(adminService.updateUserRole(id, role, departmentId));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    // DEPARTMENTS

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(adminService.getAllDepartments());
    }

    @GetMapping("/departments/active")
    public ResponseEntity<List<Department>> getActiveDepartments() {
        return ResponseEntity.ok(adminService.getActiveDepartments());
    }

    @GetMapping("/departments/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getDepartmentById(id));
    }

    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.status(201).body(adminService.createDepartment(request));
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<Department> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.ok(adminService.updateDepartment(id, request));
    }

    @PutMapping("/departments/{id}/toggle")
    public ResponseEntity<Department> toggleDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleDepartmentActive(id));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<String> deleteDepartment(@PathVariable Long id) {
        adminService.deleteDepartment(id);
        return ResponseEntity.ok("Department deleted successfully");
    }
}