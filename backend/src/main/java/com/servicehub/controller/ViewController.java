package com.servicehub.controller;

import com.servicehub.exception.NotFoundException;
import com.servicehub.model.User;
import com.servicehub.model.enums.Role;
import com.servicehub.service.AdminService;
import com.servicehub.service.DashboardService;
import com.servicehub.service.ServiceRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.servicehub.model.enums.RequestStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;
import com.servicehub.dto.ServiceRequestDto;
import com.servicehub.dto.ServiceRequestResponse;
import com.servicehub.model.enums.Priority;
import com.servicehub.model.enums.RequestCategory;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final DashboardService dashboardService;
    private final ServiceRequestService requestService;
    private final AdminService adminService;

    private static final String USER_ATTR = "user";
    private static final String DEPARTMENTS_ATTR = "departments";
    private static final String TICKETS_ATTR = "tickets";
    private static final String STATS_ATTR = "stats";

    // ── INDEX → Landing Page ─────────────────────────────────────────────
    @GetMapping("/")
    public String index(@AuthenticationPrincipal User user, Model model) {
        if (user != null) {
            model.addAttribute("user", user);
        }
        model.addAttribute("activePage", "home");
        return "index";
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ADMIN ROUTES
    // ══════════════════════════════════════════════════════════════════════

    /** Admin home — overview of users, departments, recent tickets */
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminHome(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute(USER_ATTR, user);
        model.addAttribute("users", adminService.getAllUsers());
        model.addAttribute(DEPARTMENTS_ATTR, adminService.getAllDepartments());
        var stats = dashboardService.getDashboardStats();
        model.addAttribute(STATS_ATTR, stats);
        model.addAttribute("recentTickets", requestService.getAllRequests().stream()
                .sorted((a,b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .collect(Collectors.toList()));
        model.addAttribute("activePage", "dashboard");
        return "admin/home";
    }

    /** Analytics — charts, SLA, trends (Admin + Agent) */
    @GetMapping("/analytics")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public String analyticsDashboard(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute(USER_ATTR, user);
        model.addAttribute(STATS_ATTR, dashboardService.getDashboardStats());
        model.addAttribute("trends", dashboardService.getTrends(7));
        model.addAttribute("activePage", "analytics");
        return "analytics";
    }


    /** Admin — all tickets */
    @GetMapping("/admin/tickets")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminTickets(@AuthenticationPrincipal User user,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model) {
        var allTickets = requestService.getAllRequests();
        
        // Sort by most recent first
        allTickets.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, allTickets.size());
        var paginatedTickets = start < allTickets.size() ? allTickets.subList(start, end) : List.of();
        
        model.addAttribute(USER_ATTR, user);
        model.addAttribute(TICKETS_ATTR, paginatedTickets);
        model.addAttribute(DEPARTMENTS_ATTR, adminService.getAllDepartments());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) allTickets.size() / size));
        model.addAttribute("totalItems", allTickets.size());

        // Pre-calculate counts to avoid SpEL parsing errors in template
        model.addAttribute("openCount", allTickets.stream()
                .filter(t -> t.getStatus() == com.servicehub.model.enums.RequestStatus.OPEN).count());
        model.addAttribute("inProgressCount", allTickets.stream()
                .filter(t -> t.getStatus() == com.servicehub.model.enums.RequestStatus.IN_PROGRESS).count());
        model.addAttribute("resolvedCount", allTickets.stream()
                .filter(t -> t.getStatus() == com.servicehub.model.enums.RequestStatus.RESOLVED).count());
        model.addAttribute("closedCount", allTickets.stream()
                .filter(t -> t.getStatus() == com.servicehub.model.enums.RequestStatus.CLOSED).count());

        model.addAttribute("activePage", "tickets");
        return "admin/tickets";
    }


    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminUsers(@AuthenticationPrincipal User user, Model model) {
        var allUsers = adminService.getAllUsers();
        model.addAttribute(USER_ATTR, user);
        model.addAttribute("users", allUsers);
        model.addAttribute(DEPARTMENTS_ATTR, adminService.getAllDepartments());

        // Pre-calculate counts to avoid SpEL stream/lambda parsing issues
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("agentCount", allUsers.stream()
                .filter(u -> "AGENT".equals(u.getRole())).count());
        model.addAttribute("regularUserCount", allUsers.stream()
                .filter(u -> "USER".equals(u.getRole())).count());

        model.addAttribute("activePage", "users");
        return "admin/users";
    }

    /** Admin — department management */
    @GetMapping("/admin/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDepartments(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute(USER_ATTR, user);
        model.addAttribute(DEPARTMENTS_ATTR, adminService.getAllDepartments());
        model.addAttribute("activePage", "departments");
        return "admin/departments";
    }

    // ══════════════════════════════════════════════════════════════════════
    //  AGENT ROUTES
    // ══════════════════════════════════════════════════════════════════════

    /** Agent home — assigned tickets, unassigned queue, SLA warnings */
    @GetMapping("/agent/dashboard")
    @PreAuthorize("hasRole('AGENT')")
    public String agentDashboard(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute(USER_ATTR, user);
        List<ServiceRequestResponse> assigned = requestService.getAssignedRequests(user).stream()
                .map(ServiceRequestResponse::toResponse).toList();
        model.addAttribute("assignedTickets", assigned);
        model.addAttribute("unassignedTickets", requestService.getUnassignedRequests(user.getDepartment(), user.getRole()).stream()
                .map(ServiceRequestResponse::toResponse).toList());
        model.addAttribute("slaBreaches", requestService.getSlaBreachedRequests(user).stream()
                .map(ServiceRequestResponse::toResponse).toList());
        model.addAttribute("slaWarnings", requestService.getSlaWarningRequests(user).stream()
                .map(ServiceRequestResponse::toResponse).toList());

        // Pre-calculate resolved count to avoid SpEL parsing issues with streams
        long resolvedToday = assigned.stream()
                .filter(t -> "RESOLVED".equals(t.getStatus()))
                .count();
        model.addAttribute("resolvedTodayCount", resolvedToday);
        model.addAttribute("activePage", "dashboard");

        return "agent/agent-dashboard";
    }

    /** Agent — ticket list (assigned + unassigned queue) */
    @GetMapping("/agent/tickets")
    @PreAuthorize("hasRole('AGENT')")
    public String agentTickets(@AuthenticationPrincipal User user,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model) {
        var assigned = requestService.getAssignedRequests(user).stream()
                .map(ServiceRequestResponse::toResponse).toList();
        var unassigned = requestService.getUnassignedRequests(user.getDepartment(), user.getRole()).stream()
                .map(ServiceRequestResponse::toResponse).toList();
        
        // Merge assigned + unassigned for full list, sorted by most recent first
        var allTickets = new java.util.ArrayList<>(assigned);
        allTickets.addAll(unassigned);
        allTickets.sort((a, b) -> {
            if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, allTickets.size());
        var paginatedTickets = start < allTickets.size() ? allTickets.subList(start, end) : List.of();

        model.addAttribute(USER_ATTR, user);
        model.addAttribute(TICKETS_ATTR, paginatedTickets);
        model.addAttribute("unassignedTickets", unassigned);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) allTickets.size() / size));
        model.addAttribute("totalItems", allTickets.size());

        // Pre-calculate counts for stat cards
        model.addAttribute("assignedCount", (long) assigned.size());
        model.addAttribute("unassignedCount", (long) unassigned.size());
        model.addAttribute("inProgressCount", allTickets.stream()
                .filter(t -> "IN_PROGRESS".equals(t.getStatus())).count());
        model.addAttribute("slaBreachCount", allTickets.stream()
                .filter(t -> t.isSlaBreached()).count());
        model.addAttribute("resolvedCount", allTickets.stream()
                .filter(t -> "RESOLVED".equals(t.getStatus())).count());

        model.addAttribute("activePage", "tickets");
        return "agent/tickets";
    }

    // ══════════════════════════════════════════════════════════════════════
    //  USER ROUTES
    // ══════════════════════════════════════════════════════════════════════

    /** User home — open requests + recently resolved */
    @GetMapping("/user/dashboard")
    @PreAuthorize("hasRole('USER')")
    public String userDashboard(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute(USER_ATTR, user);
        model.addAttribute("openRequests", requestService.getOpenRequestsForUser(user));
        model.addAttribute("resolvedRequests", requestService.getResolvedRequestsForUser(user));
        model.addAttribute("activePage", "dashboard");
        return "users/user-dashboard";
    }

    /** User — my tickets + submit new */
    @GetMapping("/user/tickets")
    @PreAuthorize("hasRole('USER')")
    public String userTickets(@AuthenticationPrincipal User user,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               Model model) {
        model.addAttribute(USER_ATTR, user);
        
        // Get all tickets for the user, sorted by most recent first
        var allTickets = requestService.getRequestsByRequester(user.getId());
        allTickets.sort((a, b) -> {
            if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });
        
        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, allTickets.size());
        var paginatedTickets = start < allTickets.size() ? allTickets.subList(start, end) : List.of();
        
        model.addAttribute(TICKETS_ATTR, paginatedTickets);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) allTickets.size() / size));
        model.addAttribute("totalItems", allTickets.size());
        model.addAttribute(DEPARTMENTS_ATTR, adminService.getAllDepartments());
        model.addAttribute("serviceRequest", new ServiceRequestDto());
        model.addAttribute("categories", RequestCategory.values());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("activePage", "tickets");
        return "users/tickets";
    }


    /** Ticket detail — role-based (user: own only, agent: assigned/unassigned, admin: all) */
    @GetMapping("/requests/{id}")
    public String requestDetail(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
        var entity = requestService.getRequestEntityById(id);
        if (user.getRole() == Role.USER && !entity.getRequester().getId().equals(user.getId())) {
            throw new NotFoundException("Request not found");
        }
        if (user.getRole() == Role.AGENT && entity.getAssignedTo() != null && !entity.getAssignedTo().getId().equals(user.getId())) {
            throw new NotFoundException("Request not found");
        }
        model.addAttribute("ticket", requestService.getRequestById(id));
        model.addAttribute("ticketEntity", entity);
        model.addAttribute(USER_ATTR, user);
        return "requests/detail";
    }
}