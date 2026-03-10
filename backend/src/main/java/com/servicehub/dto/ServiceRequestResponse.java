package com.servicehub.dto;

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
}
