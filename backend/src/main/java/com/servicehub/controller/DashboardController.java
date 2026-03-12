package com.servicehub.controller;

import com.servicehub.dto.DashboardStatsResponse;
import com.servicehub.dto.DashboardTrendsResponse;
import com.servicehub.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Analytics and SLA performance metrics for Admin and Agent dashboards")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(
            summary     = "Get dashboard summary stats",
            description = "Returns system-wide statistics including total requests, open tickets, " +
                    "SLA compliance rate, and average resolution time. " +
                    "Accessible by ADMIN and AGENT roles."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin or Agent role required")
    })
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    @Operation(
            summary     = "Get SLA statistics",
            description = "Returns detailed SLA metrics including compliance rate, breached tickets, " +
                    "and response/resolution SLA breakdowns. Admin only."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SLA statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin role required")
    })
    @GetMapping("/sla")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStatsResponse> getSlaStats() {
        return ResponseEntity.ok(dashboardService.getSlaStas());
    }

    @Operation(
            summary     = "Get request volume trends",
            description = "Returns daily ticket volume over the specified period (7 or 30 days). " +
                    "Used to render the trend chart on the analytics dashboard."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trend data retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid period parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin or Agent role required")
    })
    @GetMapping("/trends")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<DashboardTrendsResponse> getTrends(
            @Parameter(description = "Number of days to include in trend (7 or 30)", example = "7")
            @RequestParam(defaultValue = "7") int period) {
        return ResponseEntity.ok(dashboardService.getTrends(period));
    }
}