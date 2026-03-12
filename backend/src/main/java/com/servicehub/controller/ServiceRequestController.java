package com.servicehub.controller;

import com.servicehub.dto.*;
import com.servicehub.model.User;
import com.servicehub.service.ServiceRequestService;
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
public class ServiceRequestController {
    private final ServiceRequestService requestService;

    // Paginated → admin only, for dashboard
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ServiceRequestResponse>> getAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(requestService.getAllRequests(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceRequestResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getRequestById(id));
    }

    @PostMapping
    public ResponseEntity<ServiceRequestResponse> create(
            @Valid @RequestBody ServiceRequestDto dto,
            @AuthenticationPrincipal User requester) {
        return ResponseEntity.ok(requestService.createRequest(dto, requester));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ServiceRequestResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request
            ) {
        return ResponseEntity.ok(requestService.updateStatus(id, request));
    }




    // DELETE /api/requests/{id} — Delete a ticket (only for requester or admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        requestService.deleteRequest(id, user);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/requests/{id} — Update a ticket (only for requester or admin)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ServiceRequestResponse> updateRequest(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequestDto dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(requestService.updateRequest(id, dto, user));
    }

    // GET /api/requests/my-requests — USER sees own tickets
    @GetMapping("/my-requests")
    public ResponseEntity<List<ServiceRequestResponse>> getMyRequests(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(requestService.getRequestsByRequester(user.getId()));
    }

    // Role-based — main method controller will call
    @GetMapping
    public ResponseEntity<List<ServiceRequestResponse>> getAll(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(requestService.getRequestsForUser(user));
    }

    // TODO: Add dashboard stats endpoint - GET /api/requests/dashboard
}
