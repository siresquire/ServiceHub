package com.servicehub.controller;

import com.servicehub.dto.*;
import com.servicehub.model.User;
import com.servicehub.service.ServiceRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Tag(name = "Service Requests", description = "Endpoints for creating and managing service requests / tickets")
public class ServiceRequestController {

    private final ServiceRequestService requestService;


    @Operation(
            summary     = "Get all requests (paginated)",
            description = "Admin only. Returns all service requests across the system with pagination support."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of service requests returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden — admin role required")
    })
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ServiceRequestResponse>> getAllPaginated(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(requestService.getAllRequests(page, size));
    }

    @Operation(
            summary     = "Get request by ID",
            description = "Retrieve a single service request by its unique ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service request retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "404", description = "Service request not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ServiceRequestResponse> getById(
            @Parameter(description = "Service request ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(requestService.getRequestById(id));
    }



    @Operation(
            summary     = "Create a new service request",
            description = "Any authenticated user can submit a new service request. " +
                    "The requester is automatically derived from the JWT token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service request created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body — validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required")
    })
    @PostMapping
    public ResponseEntity<ServiceRequestResponse> create(
            @Valid @RequestBody ServiceRequestDto dto,
            @AuthenticationPrincipal User requester) {
        return ResponseEntity.ok(requestService.createRequest(dto, requester));
    }



    @Operation(
            summary     = "Update request status",
            description = "Agent or Admin can update the status of a service request " +
                    "(e.g. OPEN → IN_PROGRESS → RESOLVED)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden — insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Service request not found")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<ServiceRequestResponse> updateStatus(
            @Parameter(description = "Service request ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(requestService.updateStatus(id, request));
    }

    @Operation(
            summary     = "Assign request to current agent",
            description = "Agent or Admin assigns a request to themselves. Sets status to ASSIGNED."
    )
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<ServiceRequestResponse> assignToMe(
            @PathVariable Long id,
            @AuthenticationPrincipal User agent) {
        return ResponseEntity.ok(requestService.updateStatus(id, agent.getId()));
    }


    @Operation(
            summary     = "Get my requests",
            description = "Returns all service requests submitted by the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of user's own service requests"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required")
    })
    @GetMapping("/my-requests")
    public ResponseEntity<List<ServiceRequestResponse>> getMyRequests(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(requestService.getRequestsByRequester(user.getId()));
    }



    @Operation(
            summary     = "Get requests based on role",
            description = "Returns service requests filtered by the caller's role: " +
                    "ADMIN sees all, AGENT sees assigned + department queue, USER sees own requests."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role-filtered list of service requests"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required")
    })
    @GetMapping
    public ResponseEntity<List<ServiceRequestResponse>> getAll(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(requestService.getRequestsForUser(user));
    }
}