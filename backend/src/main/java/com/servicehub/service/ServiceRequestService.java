package com.servicehub.service;

import com.servicehub.dto.*;
import com.servicehub.model.*;
import com.servicehub.model.enums.*;
import com.servicehub.repository.*;
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
                .orElseThrow(() -> new RuntimeException("Request not found"));
        req.setStatus(RequestStatus.valueOf(update.getNewStatus()));
        req.setAssignedTo(agent);
        req.setUpdatedAt(LocalDateTime.now());
        if (update.getNewStatus().equals("RESOLVED")) {
            req.setResolvedAt(LocalDateTime.now());
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
            case AGENT -> getRequestsByDepartment(user.getDepartment());
            case USER -> getRequestsByRequester(user.getId());
        };
    }

    // TODO: Implement assignRequest(Long requestId, Long agentId)
    // TODO: Implement getDashboardStats()
    // TODO: Implement SLA breach detection

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
