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
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Retrieves all service requests with pagination, ordered by creation date descending.
     * Result is cached per page/size combination for fast repeated dashboard loads.
     *
     * @param page zero-based page index
     * @param size number of records per page
     * @return paginated list of service request responses
     */
    @Transactional(readOnly = true)
    public Page<ServiceRequestResponse> getAllRequests(int page, int size) {
        return requestRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(ServiceRequestResponse::toResponse);
    }

    /**
     * Retrieves a single service request by its unique ID.
     * Cached individually by ID for fast repeated lookups.
     *
     * @param id the unique identifier of the service request
     * @return the matching service request response
     * @throws RuntimeException if no request is found with the given ID
     */
    @Transactional(readOnly = true)
    public ServiceRequestResponse getRequestById(Long id) {
        return ServiceRequestResponse.toResponse(requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found")));
    }

    /**
     * Returns the raw ServiceRequest entity by ID.
     * Used where direct entity access is needed (e.g. permission checks in ViewController).
     * Unlike {@link #getRequestById(Long)} which returns a DTO,
     * this returns the full entity for field-level access.
     *
     * @param id the service request ID
     * @return the ServiceRequest entity
     * @throws NotFoundException if no request exists with the given ID
     */
    public ServiceRequest getRequestEntityById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Request not found: " + id));
    }

    /**
     * Creates a new service request on behalf of the authenticated user.
     * Looks up the department by ID, builds the request entity with default values,
     * applies SLA deadlines based on category and priority, then persists it.
     * Evicts all related caches so dashboard counts stay accurate.
     *
     * @param dto       the request payload containing title, description, category, priority, and departmentId
     * @param requester the authenticated user submitting the request
     * @return the created service request as a response DTO
     * @throws RuntimeException if the specified department does not exist
     */

    @Transactional
    public ServiceRequestResponse createRequest(ServiceRequestDto dto, User requester) {
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new NotFoundException("Department not found: " + dto.getDepartmentId()));
        ServiceRequest req = ServiceRequest.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(RequestCategory.valueOf(dto.getCategory()))
                .priority(Priority.valueOf(dto.getPriority()))
                .status(RequestStatus.OPEN)
                .requester(requester)
                .department(department)       // ← was missing
                .resolved(false)              // ← fixes NOT NULL constraint
                .slaBreached(false)
                .resolvedAt(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        req.setResponseSlaDeadline(slaPolicyService.getResponseSlaDeadline(req.getCategory(), req.getPriority()));
        req.setResolutionSlaDeadline(slaPolicyService.getResolutionSlaDeadline(req.getCategory(), req.getPriority()));
        return ServiceRequestResponse.toResponse(requestRepository.save(req));
    }

    /**
     * Update service request status with validation of allowed transitions and agent assignment for ASSIGNED status
     * @param id Service request id to update
     * @param update DTO containing the new status to update to
     * @param agent (optional) agent to assign if the new status is ASSIGNED. Must be provided if updating to ASSIGNED, otherwise ignored
     * @return updated service request response
     */
    @Transactional
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

        return ServiceRequestResponse.toResponse(requestRepository.save(req));
    }

    /**
     * Returns all service requests submitted by a specific user.
     * Cached by user ID for fast repeated access on the user dashboard.
     *
     * @param userId the ID of the user whose requests to retrieve
     * @return list of service request responses for the given user
     */
    @Transactional
    public List<ServiceRequestResponse> getRequestsByRequester(Long userId) {
        return requestRepository.findByRequesterId(userId)
                .stream().map(ServiceRequestResponse::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public List<ServiceRequestResponse> getRequestsByDepartment(String department, User user) {
        if (user.getRole() == Role.ADMIN) {
            return requestRepository.findByDepartment_Name(department)
                    .stream().map(ServiceRequestResponse::toResponse).collect(Collectors.toList());
        }
        
        // An agent's department queue should only show items assigned to them
        // or OPEN items in the department that are unassigned
        List<ServiceRequest> assigned = requestRepository.findByAssignedTo(user);
        List<ServiceRequest> unassignedOpen = requestRepository.findByDepartmentAndAssignedToIsNullAndStatus(user.getDepartment(), RequestStatus.OPEN);

        var combined = new java.util.ArrayList<>(assigned);
        combined.addAll(unassignedOpen);

        return combined.stream().map(ServiceRequestResponse::toResponse).collect(Collectors.toList());
    }

    /**
     * Returns service requests filtered by the caller's role:
     * <ul>
     *   <li>ADMIN — all requests in the system</li>
     *   <li>AGENT — all requests in the agent's department</li>
     *   <li>USER — only requests submitted by this user</li>
     * </ul>
     *
     * @param user the authenticated user
     * @return role-filtered list of service request responses
     */
    @Transactional
    public List<ServiceRequestResponse> getRequestsForUser(User user) {
        return switch (user.getRole()) {
            case ADMIN -> requestRepository.findAll()
                    .stream().map(ServiceRequestResponse::toResponse).collect(Collectors.toList());
            case AGENT -> getRequestsByDepartment( user.getDepartment() != null ? user.getDepartment().getName() : null, user);
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

    /**
     * Validates if the status transition is allowed based on the current status and the desired new status.
     * @param current current status of the request in the db
     * @param newStatus new status that the user wants to update to
     * @throws BadRequestException if the new status is the same as the current status
     * @throws InvalidServiceRequestTransition if the transition is not allowed based on the defined workflow
     */
    @Transactional
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
    /**
     * Returns open and active requests for a specific user.
     * Includes tickets with status OPEN, ASSIGNED, or IN_PROGRESS.
     * Used to populate the "Active Requests" panel on the user dashboard.
     *
     * @param user the authenticated user
     * @return list of open/in-progress service requests for the user
     */
    @Transactional
    public List<ServiceRequest> getOpenRequestsForUser(User user) {
        return requestRepository.findByRequesterAndStatusIn(
                user,
                List.of(RequestStatus.OPEN, RequestStatus.IN_PROGRESS, RequestStatus.ASSIGNED)
        );
    }
    /**
     * Returns resolved and closed requests for a specific user.
     * Includes tickets with status RESOLVED or CLOSED.
     * Used to populate the "Resolved Requests" panel on the user dashboard.
     *
     * @param user the authenticated user
     * @return list of resolved/closed service requests for the user
     */
    @Transactional
    public List<ServiceRequest> getResolvedRequestsForUser(User user) {
        return requestRepository.findByRequesterAndStatusIn(
                user,
                List.of(RequestStatus.RESOLVED, RequestStatus.CLOSED)
        );
    }


    @Transactional
    public List<ServiceRequest> getAllRequests() {
        return requestRepository.findAll();
    }

    /**
     * Returns all service requests currently assigned to a specific agent.
     * Used to populate the agent's personal ticket queue.
     *
     * @param agent the agent user whose assigned requests to retrieve
     * @return list of service requests assigned to the agent
     */
    @Transactional
    public List<ServiceRequest> getAssignedRequests(User agent) {
        return requestRepository.findByAssignedTo(agent);
    }

    @Transactional
    public List<ServiceRequest> getUnassignedRequests() {
        return requestRepository.findByAssignedToIsNull();
    }

    @Transactional
    public List<ServiceRequest> getUnassignedRequests(Department department, Role role) {
        if (department == null) return getUnassignedRequests();
        if (role == Role.ADMIN) {
            // Admin sees all unassigned in department
            return requestRepository.findByDepartmentAndAssignedToIsNull(department);
        }
        // Agents see only OPEN unassigned in department
        return requestRepository.findByDepartmentAndAssignedToIsNullAndStatus(department, RequestStatus.OPEN);
    }

    /**
     * Returns all service requests where the SLA deadline has been breached.
     * Not cached — SLA status changes frequently via the SLA engine scheduler.
     *
     * @return list of SLA-breached service requests
     */
    @Transactional
    public List<ServiceRequest> getSlaBreachedRequests() {
        return requestRepository.findBySlaBreachedTrue();
    }

    @Transactional
    public List<ServiceRequest> getSlaBreachedRequests(User user) {
        if (user == null || user.getRole() == Role.ADMIN) return getSlaBreachedRequests();
        if (user.getDepartment() == null) return getSlaBreachedRequests();
        
        List<ServiceRequest> assigned = requestRepository.findByAssignedToAndSlaBreachedTrue(user);
        List<ServiceRequest> unassignedOpen = requestRepository.findByDepartmentAndAssignedToIsNullAndStatusAndSlaBreachedTrue(user.getDepartment(), RequestStatus.OPEN);
        
        var combined = new java.util.ArrayList<>(assigned);
        combined.addAll(unassignedOpen);
        return combined;
    }

    /**
     * Returns service requests whose resolution SLA deadline falls within the next 2 hours.
     * Used to surface early warnings on agent and admin dashboards.
     * Not cached — time-sensitive, must always reflect current state.
     *
     * @return list of service requests approaching SLA deadline
     */
    @Transactional
    public List<ServiceRequest> getSlaWarningRequests() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.plusHours(2);
        return requestRepository.findSlaWarnings(now, cutoff);
    }

    @Transactional
    public List<ServiceRequest> getSlaWarningRequests(User user) {
        if (user == null || user.getRole() == Role.ADMIN) return getSlaWarningRequests();
        if (user.getDepartment() == null) return getSlaWarningRequests();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.plusHours(2);
        
        List<ServiceRequest> assigned = requestRepository.findSlaWarningsForAgent(user, now, cutoff);
        List<ServiceRequest> unassignedOpen = requestRepository.findSlaWarningsForDepartmentUnassigned(user.getDepartment(), RequestStatus.OPEN, now, cutoff);
        
        var combined = new java.util.ArrayList<>(assigned);
        combined.addAll(unassignedOpen);
        return combined;
    }



}
