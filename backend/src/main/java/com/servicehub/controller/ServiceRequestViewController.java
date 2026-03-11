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
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam Long departmentId,
            @RequestParam(defaultValue = "MEDIUM") String priority,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            var dept = adminService.getDepartmentById(departmentId);
            var dto = new ServiceRequestDto();
            dto.setTitle(title);
            dto.setDescription(description != null ? description : "");
            dto.setCategory(dept.getCategory().name());
            dto.setPriority(priority);
            dto.setDepartmentId(departmentId);

            requestService.createRequest(dto, user);
            redirectAttributes.addFlashAttribute("success", "Request submitted successfully!");
            return "redirect:/user/tickets";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/user/tickets#new";
        }
    }
}
