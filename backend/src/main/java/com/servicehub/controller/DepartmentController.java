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

    // Used by register form — no auth needed




}