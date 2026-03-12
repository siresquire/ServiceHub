package com.servicehub.controller;

import com.servicehub.model.Department;
import com.servicehub.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final AdminService adminService;

    /**
     * Public endpoint — used by registration form and agent role-assignment modal.
     */
    @GetMapping
    public ResponseEntity<List<Department>> getAll() {
        return ResponseEntity.ok(adminService.getAllDepartments());
    }
}