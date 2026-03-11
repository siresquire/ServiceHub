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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final DashboardService dashboardService;
    private final ServiceRequestService requestService;
    private final AdminService adminService;

    // ── INDEX → redirect to login ──────────────────────────────────────────
    @GetMapping("/")
    public String index() {
        return "redirect:/auth/login";
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ADMIN ROUTES
    // ══════════════════════════════════════════════════════════════════════

    /** Admin home — overview of users, departments, recent tickets */
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminHome(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        model.addAttribute("departments", adminService.getAllDepartments());
        model.addAttribute("stats", dashboardService.getDashboardStats());
        return "admin/home";
    }

    /** Admin analytics — charts, SLA, trends */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAnalytics(Model model) {
        model.addAttribute("stats", dashboardService.getDashboardStats());
        model.addAttribute("trends", dashboardService.getTrends(7));
        return "dashboard";
    }


    /** Admin — all tickets */
    @GetMapping("/admin/tickets")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminTickets(Model model) {
        model.addAttribute("tickets", requestService.getAllRequests());
        model.addAttribute("departments", adminService.getAllDepartments());
        return "admin/tickets";
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

    /** Agent — ticket list (assigned + unassigned queue) */
    @GetMapping("/agent/tickets")
    @PreAuthorize("hasRole('AGENT')")
    public String agentTickets(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("tickets", requestService.getAssignedRequests(user));
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
        model.addAttribute("user", user);
        return "requests/detail";
    }
}