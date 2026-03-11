package com.servicehub.controller;

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

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final DashboardService dashboardService;
    private final ServiceRequestService requestService;
    private final AdminService adminService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAnalytics(Model model) {
        model.addAttribute("stats", dashboardService.getDashboardStats());
        model.addAttribute("trends", dashboardService.getTrends(7));
        return "dashboard";
    }
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminHome(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        model.addAttribute("departments", adminService.getAllDepartments());
        return "admin/home";
    }

    @GetMapping("/agent/dashboard")
    @PreAuthorize("hasRole('AGENT')")
    public String agentDashboard(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("requests", requestService.getRequestsForUser(user));
        model.addAttribute("user", user);
        return "agent-dashboard";
    }

    @GetMapping("/user/dashboard")
    @PreAuthorize("hasRole('USER')")
    public String userDashboard(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("requests", requestService.getRequestsForUser(user));
        model.addAttribute("user", user);
        return "user-dashboard";
    }

    /** Admin — user management */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminUsers(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        model.addAttribute("departments", adminService.getAllDepartments());
        return "admin/users";
    }

    /** Admin — department management */
    @GetMapping("/admin/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDepartments(Model model) {
        model.addAttribute("departments", adminService.getAllDepartments());
        return "admin/departments";
    }

    // ══════════════════════════════════════════════════════════════════════
    //  AGENT ROUTES
    // ══════════════════════════════════════════════════════════════════════

    /** Agent home — assigned tickets, unassigned queue, SLA warnings */
    @GetMapping("/agent/dashboard")
    @PreAuthorize("hasRole('AGENT')")
    public String agentDashboard(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("assignedTickets", requestService.getAssignedRequests(user));
        model.addAttribute("unassignedTickets", requestService.getUnassignedRequests());
        model.addAttribute("slaBreaches", requestService.getSlaBreachedRequests());
        model.addAttribute("slaWarnings", requestService.getSlaWarningRequests());
        return "/agent/agent-dashboard";
    }

    /** Agent — ticket list (assigned + department queue) */
    @GetMapping("/agent/tickets")
    @PreAuthorize("hasRole('AGENT')")
    public String agentTickets(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("tickets", requestService.getRequestsForUser(user));
        model.addAttribute("unassignedTickets", requestService.getUnassignedRequests());
        return "agent/tickets";
    }

    // ══════════════════════════════════════════════════════════════════════
    //  USER ROUTES
    // ══════════════════════════════════════════════════════════════════════

    /** User home — open requests + recently resolved */
    @GetMapping("/user/dashboard")
    @PreAuthorize("hasRole('USER')")
    public String userDashboard(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("openRequests", requestService.getOpenRequestsForUser(user));
        model.addAttribute("resolvedRequests", requestService.getResolvedRequestsForUser(user));
        return "/users/user-dashboard";
    }

    /** User — my tickets + submit new */
    @GetMapping("/user/tickets")
    @PreAuthorize("hasRole('USER')")
    public String userTickets(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("tickets", requestService.getRequestsForUser(user));
        model.addAttribute("departments", adminService.getAllDepartments());
        return "users/tickets";
    }
}