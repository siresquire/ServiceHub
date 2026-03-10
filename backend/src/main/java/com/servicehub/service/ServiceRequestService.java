package com.servicehub.service;

import com.servicehub.dto.*;
import com.servicehub.exception.BadRequestException;
import com.servicehub.exception.InvalidServiceRequestTransition;
import com.servicehub.exception.NotFoundException;
import com.servicehub.model.*;
import com.servicehub.model.enums.*;
import com.servicehub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {
    private final ServiceRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

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
                .status(RequestStatus.SUBMITTED)
                .requester(requester)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return toResponse(requestRepository.save(req));
    }

    public ServiceRequestResponse updateStatus(Long id, StatusUpdateRequest update, User agent) {
        ServiceRequest req = requestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Request not found"));
        this.validateStatusTransition(req.getStatus(), update.getNewStatus());
        req.setStatus(update.getNewStatus());
        req.setAssignedTo(agent);
        req.setUpdatedAt(LocalDateTime.now());
        if (update.getNewStatus().equals(RequestStatus.RESOLVED)) {
            req.setResolvedAt(LocalDateTime.now());
        }
        return toResponse(requestRepository.save(req));
    }


    // TODO: Implement assignRequest(Long requestId, Long agentId)
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
            case SUBMITTED:
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
                .createdAt(req.getCreatedAt())
                .updatedAt(req.getUpdatedAt())
                .resolvedAt(req.getResolvedAt())
                .build();
    }
}
