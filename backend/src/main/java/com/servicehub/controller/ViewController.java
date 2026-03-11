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
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ViewController {

    private final DashboardService dashboardService;
    private final ServiceRequestService requestService;
    private final AdminService adminService;

    // ADMIN
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        model.addAttribute("stats", dashboardService.getDashboardStats());
        model.addAttribute("trends", dashboardService.getTrends(7));
        return "dashboard";
    }

    // AGENT
    @GetMapping("/agent/dashboard")
    @PreAuthorize("hasRole('AGENT')")
    public String agentDashboard(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("requests", requestService.getRequestsForUser(user));
        model.addAttribute("user", user);
        return "agent-dashboard";
    }

    // USER
    @GetMapping("/user/dashboard")
    @PreAuthorize("hasRole('USER')")
    public String userDashboard(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("requests", requestService.getRequestsForUser(user));
        model.addAttribute("user", user);
        return "user-dashboard";
    }

    // ── ADMIN USERS PAGE ──
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminUsers(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        return "admin/users";
    }

    // ── ADMIN DEPARTMENTS PAGE ──
    @GetMapping("/admin/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDepartments(Model model) {
        model.addAttribute("departments", adminService.getAllDepartments());
        return "admin/departments";
    }
}
