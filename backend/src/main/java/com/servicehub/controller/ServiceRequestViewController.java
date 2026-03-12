package com.servicehub.controller;

import com.servicehub.dto.ServiceRequestDto;
import com.servicehub.model.User;
import com.servicehub.service.AdminService;
import com.servicehub.service.ServiceRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user/requests")
@RequiredArgsConstructor
public class ServiceRequestViewController {

    private final ServiceRequestService requestService;
    private final AdminService adminService;

    /**
     * Form-based submission of a new service request (for USER role).
     * Maps departmentId to category via department's category.
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('USER')")
    public String submitRequest(
            @ModelAttribute ServiceRequestDto dto,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes) {
        try {
            if (dto.getDepartmentId() != null) {
                var dept = adminService.getDepartmentById(dto.getDepartmentId());
                dto.setCategory(dept.getCategory().name());
            } else if (dto.getCategory() == null && user.getDepartment() != null) {
                // Default to user's department if nothing specified
                dto.setDepartmentId(user.getDepartment().getId());
                dto.setCategory(user.getDepartment().getCategory().name());
            }

            requestService.createRequest(dto, user);
            redirectAttributes.addAttribute("success", "Request submitted successfully!");
            return "redirect:/user/tickets";
        } catch (RuntimeException ex) {
            redirectAttributes.addAttribute("error", ex.getMessage());
            return "redirect:/user/tickets#new";
        }
    }

}
