package com.servicehub.controller;


import com.servicehub.dto.DashboardStatsResponse;
import com.servicehub.dto.DashboardTrendsResponse;
import com.servicehub.model.User;
import com.servicehub.service.AdminService;
import com.servicehub.service.DashboardService;
import com.servicehub.service.ServiceRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final DashboardService dashboardService;
    private final ServiceRequestService requestService;
    private final AdminService adminService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        model.addAttribute("stats", dashboardService.getDashboardStats());
        model.addAttribute("trends", dashboardService.getTrends(7));
        return "dashboard";
    }
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminHome(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        model.addAttribute("departments", adminService.getAllDepartments());

        // Add admin dashboard card statistics
        var stats = dashboardService.getAdminDashboardCardStats();
        model.addAttribute("totalTickets", stats.get("totalTickets"));
        model.addAttribute("openTickets", stats.get("openTickets"));
        model.addAttribute("slaBreaches", stats.get("slaBreaches"));
        model.addAttribute("avgResolutionHours", stats.get("avgResolutionHours"));

        return "admin/home";
    }

    @GetMapping("/agent/dashboard")
    @PreAuthorize("hasRole('AGENT')")
    public String agentDashboard(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("requests", requestService.getRequestsForUser(user));
        model.addAttribute("user", user);

        // Add agent dashboard statistics
        var stats = requestService.getAgentDashboardStats(user);
        model.addAttribute("assignedToMe", stats.get("assignedToMe"));
        model.addAttribute("unassigned", stats.get("unassigned"));
        model.addAttribute("slaBreaches", stats.get("slaBreaches"));
        model.addAttribute("slaWarnings", stats.get("slaWarnings"));

        return "agent-dashboard";
    }

    @GetMapping("/user/dashboard")
    @PreAuthorize("hasRole('USER')")
    public String userDashboard(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("requests", requestService.getRequestsForUser(user));
        model.addAttribute("user", user);

        // Add user dashboard statistics
        var stats = requestService.getUserDashboardStats(user);
        model.addAttribute("openRequests", stats.get("openRequests"));
        model.addAttribute("resolvedRequests", stats.get("resolvedRequests"));
        model.addAttribute("slaBreaches", stats.get("slaBreaches"));
        model.addAttribute("totalRequests", stats.get("totalRequests"));

        return "user-dashboard";
    }

    // ── ADMIN USERS PAGE ──
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminUsers(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        return "admin/users";
    }

    // ── ADMIN TICKETS PAGE ──
    @GetMapping("/admin/tickets")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminTickets(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("requests", requestService.getRequestsForUser(user));
        return "admin/tickets";
    }

    // ── ADMIN DEPARTMENTS PAGE ──
    @GetMapping("/admin/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDepartments(Model model) {
        model.addAttribute("departments", adminService.getAllDepartments());
        return "admin/departments";
    }

    // ── USER TICKET CREATION PAGE ──
    @GetMapping("/tickets/new")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'AGENT')")
    public String newTicketForm(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("categories", java.util.List.of("IT_SUPPORT", "FACILITIES", "HR", "FINANCE", "OTHER"));
        model.addAttribute("priorities", java.util.List.of("LOW", "MEDIUM", "HIGH", "CRITICAL"));
        model.addAttribute("user", user);
        return "tickets/new";
    }

    // ── USER MY TICKETS PAGE ──
    @GetMapping("/tickets/my")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'AGENT')")
    public String myTickets(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("requests", requestService.getRequestsByRequester(user.getId()));
        model.addAttribute("user", user);
        return "tickets/my";
    }
}
