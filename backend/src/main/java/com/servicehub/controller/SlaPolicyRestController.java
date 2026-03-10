package com.servicehub.controller;

import com.servicehub.dto.ServerResponse;
import com.servicehub.dto.SlaPolicyCreate;
import com.servicehub.dto.SlaPolicyDto;
import com.servicehub.dto.SlaPolicyUpdate;
import com.servicehub.model.enums.RequestCategory;
import com.servicehub.service.SlaPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sla-policies")
@Tag(name = "SLA Policy Management", description = "Endpoints for managing SLA policies")
@RequiredArgsConstructor
public class SlaPolicyRestController {

  private final SlaPolicyService slaPolicyService;

  @GetMapping("{id}")
  @Operation(summary = "Get SLA Policy by ID", description = "Retrieve a specific SLA policy by its unique identifier.")
  @ApiResponse(responseCode = "200", description = "SLA Policy retrieved successfully")
  @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
  @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
  @ApiResponse(responseCode = "404", description = "SLA Policy not found")
  public ResponseEntity<ServerResponse<SlaPolicyDto>> getPolicyById(@PathVariable Long id) {
    var policy = slaPolicyService.getById(id);
    return ResponseEntity.ok(new ServerResponse<>("SLA Policy retrieved successfully", policy));
  }

  @GetMapping()
  @Operation(summary = "Get All SLA Policies", description = "Retrieve a paginated list of all SLA policies.")
  @ApiResponse(responseCode = "200", description = "SLA Policies retrieved successfully")
  @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
  @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
  public ResponseEntity<ServerResponse<Page<SlaPolicyDto>>> getAllPolicies(Pageable page) {
    var policies = slaPolicyService.getAll(page);
    return ResponseEntity.ok(new ServerResponse<>("SLA Policies retrieved successfully", policies));
  }

  @GetMapping("/categories/{category}")
  @Operation(summary = "Get SLA Policies by Category", description = "Retrieve SLA policies filtered by request category.")
  @ApiResponse(responseCode = "200", description = "SLA Policies retrieved successfully")
  @ApiResponse(responseCode = "400", description = "Bad Request - Invalid category")
  @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
  @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
  public ResponseEntity<ServerResponse<List<SlaPolicyDto>>> getPoliciesByCategory(@PathVariable RequestCategory category) {
    var policies = slaPolicyService.getByCategory(category);
    return ResponseEntity.ok(new ServerResponse<>("SLA Policies retrieved successfully", policies));
  }

  @PostMapping
  @Operation(summary = "Create SLA Policy", description = "Create a new SLA policy with specified parameters.")
  @ApiResponse(responseCode = "200", description = "SLA Policy created successfully")
  @ApiResponse(responseCode = "400", description = "Bad Request - Validation errors or duplicate policy")
  @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
  @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
  public ResponseEntity<ServerResponse<SlaPolicyDto>> createPolicy(@RequestBody SlaPolicyCreate dto) {
    var policy = slaPolicyService.create(dto);
    return ResponseEntity.ok(new ServerResponse<>("SLA Policy created successfully", policy));
  }

  @PatchMapping("{id}")
  @Operation(summary = "Update SLA Policy", description = "Update an existing SLA policy's parameters.")
  @ApiResponse(responseCode = "200", description = "SLA Policy updated successfully")
  @ApiResponse(responseCode = "400", description = "Bad Request - Validation errors or duplicate policy")
  @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
  @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
  @ApiResponse(responseCode = "404", description = "SLA Policy not found")
  public ResponseEntity<ServerResponse<SlaPolicyDto>> updatePolicy(@PathVariable Long id, @RequestBody SlaPolicyUpdate dto) {
    var policy = slaPolicyService.update(id,dto);
    return ResponseEntity.ok(new ServerResponse<>("SLA Policy updated successfully", policy));
  }

  @DeleteMapping("{id}")
  @Operation(summary = "Delete SLA Policy", description = "Delete an existing SLA policy by its unique identifier.")
  @ApiResponse(responseCode = "200", description = "SLA Policy deleted successfully")
  @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
  @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
  @ApiResponse(responseCode = "404", description = "SLA Policy not found")
  public ResponseEntity<ServerResponse<Void>> deletePolicy(@PathVariable Long id) {
    slaPolicyService.delete(id);
    return ResponseEntity.ok(new ServerResponse<>("SLA Policy deleted successfully", null));
  }


}
