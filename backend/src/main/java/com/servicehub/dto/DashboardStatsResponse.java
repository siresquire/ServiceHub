package com.servicehub.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardStatsResponse {
    private Long totalRequests;
    private Long openRequests;
    private Long resolvedRequests;
    private Double avgResolutionHours;
    private Double slaComplianceRate;
    private Map<String, Long> requestsByCategory;
    private Map<String, Long> requestsByPriority;
    private Map<String,Long> requestsByStatus;
    private Map<String,Long>  slaByCategory;
}
