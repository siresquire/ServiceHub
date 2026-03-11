package com.servicehub.service;

import com.servicehub.dto.*;
import com.servicehub.exception.BadRequestException;
import com.servicehub.exception.InvalidServiceRequestTransition;
import com.servicehub.exception.NotFoundException;
import com.servicehub.model.*;
import com.servicehub.model.enums.*;
import com.servicehub.repository.*;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {
    private final ServiceRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final SlaPolicyService slaPolicyService;

    public Page<ServiceRequestResponse> getAllRequests(int page, int size) {
        return requestRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(this::toResponse);
    }

    public ServiceRequestResponse getRequestById(Long id) {
        return toResponse(requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found")));
    }

    public ServiceRequestResponse createRequest(ServiceRequestDto dto, User requester) {
        ServiceRequest req = ServiceRequest.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(RequestCategory.valueOf(dto.getCategory()))
                .priority(Priority.valueOf(dto.getPriority()))
                .status(RequestStatus.OPEN)
                .requester(requester)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        req.setResponseSlaDeadline(slaPolicyService.getResponseSlaDeadline(req.getCategory(), req.getPriority()));
        req.setResolutionSlaDeadline(slaPolicyService.getResolutionSlaDeadline(req.getCategory(), req.getPriority()));
        return toResponse(requestRepository.save(req));
    }

    /**
     * Update service request status with validation of allowed transitions and agent assignment for ASSIGNED status
     * @param id Service request id to update
     * @param update DTO containing the new status to update to
     * @param agent (optional) agent to assign if the new status is ASSIGNED. Must be provided if updating to ASSIGNED, otherwise ignored
     * @return updated service request response
     */
    public ServiceRequestResponse updateStatus(@NotNull Long id, @NotNull StatusUpdateRequest update, User agent) {
        ServiceRequest req = requestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        // Validations: Transition rules, agent assignment for ASSIGNED status
        this.validateStatusTransition(req.getStatus(), update.getNewStatus());
        if(update.getNewStatus().equals(RequestStatus.ASSIGNED) && agent == null) {
            throw new BadRequestException("Agent must be provided when assigning a request");
        }

        req.setStatus(update.getNewStatus());

        // Track resolved time for SLA reporting
        if (update.getNewStatus().equals(RequestStatus.RESOLVED)) {
            req.setResolvedAt(LocalDateTime.now());
        }

        if (update.getNewStatus().equals(RequestStatus.ASSIGNED)) {
            req.setAssignedTo(agent);
            req.setAssignedAt(LocalDateTime.now());
        }

        return toResponse(requestRepository.save(req));
    }

    // For USER — own tickets only
    public List<ServiceRequestResponse> getRequestsByRequester(Long userId) {
        return requestRepository.findByRequesterId(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // For AGENT — department tickets only
    public List<ServiceRequestResponse> getRequestsByDepartment(String department) {
        return requestRepository.findByDepartment_Name(department)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Role-based — main method controller will call
    public List<ServiceRequestResponse> getRequestsForUser(User user) {
        return switch (user.getRole()) {
            case ADMIN -> requestRepository.findAll()
                    .stream().map(this::toResponse).collect(Collectors.toList());
            case AGENT -> getRequestsByDepartment(user.getDepartment().getName());
            case USER -> getRequestsByRequester(user.getId());
        };
    }

    /**
     * Helper for assign a request to an agent which internally calls the updateStatus
     * @param id Service request id to update
     * @param agentId agent to assign the request to. The status will be updated to ASSIGNED and the agent will be set as the assignedTo field of the request
     * @return updated service request response
     */
    public ServiceRequestResponse updateStatus(Long id, Long agentId) {
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new NotFoundException("Agent not found"));

        var updateService = new StatusUpdateRequest();
        updateService.setNewStatus(RequestStatus.ASSIGNED);

        return updateStatus(id, updateService , agent);
    }

    public ServiceRequestResponse updateStatus(Long id, StatusUpdateRequest update) {
        return updateStatus(id, update , null);
    }



    // TODO: Implement getRequestsByRequester(Long userId)
    // TODO: Implement getDashboardStats()
    // TODO: Implement SLA breach detection

    /**
     * Validates if the status transition is allowed based on the current status and the desired new status.
     * @param current current status of the request in the db
     * @param newStatus new status that the user wants to update to
     * @throws BadRequestException if the new status is the same as the current status
     * @throws InvalidServiceRequestTransition if the transition is not allowed based on the defined workflow
     */
    private void validateStatusTransition(RequestStatus current, RequestStatus newStatus) {

        if(current.equals(newStatus)) {
            throw new BadRequestException("Request is already in the desired status");
        }

        switch (current) {
            case OPEN:
                if (newStatus != RequestStatus.ASSIGNED && newStatus != RequestStatus.CLOSED) {
                    throw new InvalidServiceRequestTransition(current, newStatus);
                }
                break;
            case ASSIGNED:
                if (newStatus != RequestStatus.IN_PROGRESS && newStatus != RequestStatus.CLOSED) {
                    throw new InvalidServiceRequestTransition(current, newStatus);
                }
                break;
            case IN_PROGRESS:
                if (newStatus != RequestStatus.RESOLVED && newStatus != RequestStatus.CLOSED) {
                    throw new BadRequestException("Invalid status transition from IN_PROGRESS to " + newStatus);
                }
                break;
            case RESOLVED,CLOSED:
                throw new InvalidServiceRequestTransition(current, newStatus);
        }
    }
    // ── USER ──────────────────────────────────────────────────────────────────

    public List<ServiceRequest> getOpenRequestsForUser(User user) {
        return requestRepository.findByRequesterAndStatusIn(
                user,
                List.of(RequestStatus.OPEN, RequestStatus.IN_PROGRESS, RequestStatus.ASSIGNED)
        );
    }

    public List<ServiceRequest> getResolvedRequestsForUser(User user) {
        return requestRepository.findByRequesterAndStatusIn(
                user,
                List.of(RequestStatus.RESOLVED, RequestStatus.CLOSED)
        );
    }

// ── ADMIN ─────────────────────────────────────────────────────────────────

    public List<ServiceRequest> getAllRequests() {
        return requestRepository.findAll();
    }

// ── AGENT ─────────────────────────────────────────────────────────────────

    public List<ServiceRequest> getAssignedRequests(User agent) {
        return requestRepository.findByAssignedTo(agent);
    }

    public List<ServiceRequest> getUnassignedRequests() {
        return requestRepository.findByAssignedToIsNull();
    }

    public List<ServiceRequest> getSlaBreachedRequests() {
        return requestRepository.findBySlaBreachedTrue();
    }

    public List<ServiceRequest> getSlaWarningRequests() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.plusHours(2);
        return requestRepository.findSlaWarnings(now, cutoff);
    }


    private ServiceRequestResponse toResponse(ServiceRequest req) {
        return ServiceRequestResponse.builder()
                .id(req.getId())
                .title(req.getTitle())
                .description(req.getDescription())
                .category(req.getCategory().name())
                .priority(req.getPriority().name())
                .status(req.getStatus().name())
                .requesterName(req.getRequester().getFullName())
                .assignedToName(req.getAssignedTo() != null ? req.getAssignedTo().getFullName() : null)
                .departmentName(req.getDepartment() != null ? req.getDepartment().getName() : null)
                .slaBreached(req.getSlaBreached() != null ? req.getSlaBreached() : false)
                .resolved(req.getResolved() != null ? req.getResolved() : false)
                .resolutionSlaDeadline(req.getResolutionSlaDeadline())
                .responseSlaDeadline(req.getResponseSlaDeadline())
                .createdAt(req.getCreatedAt())
                .updatedAt(req.getUpdatedAt())
                .resolvedAt(req.getResolvedAt())
                .build();
    }
}
