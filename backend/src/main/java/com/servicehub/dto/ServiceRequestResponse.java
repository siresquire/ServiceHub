package com.servicehub.dto;

import com.servicehub.model.ServiceRequest;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ServiceRequestResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String priority;
    private String status;
    private String departmentName;
    private String assignedToName;
    private String requesterName;
    private LocalDateTime resolutionSlaDeadline;
    private LocalDateTime responseSlaDeadline;
    private boolean slaBreached;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private boolean resolved;

    public static ServiceRequestResponse toResponse(ServiceRequest req) {
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
