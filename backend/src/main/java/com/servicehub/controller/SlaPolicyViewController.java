package com.servicehub.controller;

import com.servicehub.dto.SlaPolicyCreate;
import com.servicehub.dto.SlaPolicyUpdate;
import com.servicehub.model.User;
import com.servicehub.model.enums.Priority;
import com.servicehub.model.enums.RequestCategory;
import com.servicehub.service.SlaPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/sla-policies")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class SlaPolicyViewController {

    private final SlaPolicyService slaPolicyService;

    @GetMapping
    public String list(@AuthenticationPrincipal User user,
                       Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size) {
        model.addAttribute("user", user);
        model.addAttribute("policies", slaPolicyService.getAll(PageRequest.of(page, size)));
        model.addAttribute("categories", RequestCategory.values());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("activePage", "sla");
        return "admin/sla-policies";
    }

    @PostMapping
    public String create(
            @RequestParam RequestCategory category,
            @RequestParam Priority priority,
            @RequestParam double responseTimeHours,
            @RequestParam double resolutionTimeHours,
            RedirectAttributes redirectAttributes) {
        try {
            var dto = SlaPolicyCreate.builder()
                    .category(category)
                    .priority(priority)
                    .responseTimeHours(responseTimeHours)
                    .resolutionTimeHours(resolutionTimeHours)
                    .build();
            slaPolicyService.create(dto);
            redirectAttributes.addFlashAttribute("success", "SLA policy created successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/sla-policies";
    }

    @PostMapping("/{id}/update")
    public String update(
            @PathVariable Long id,
            @RequestParam double responseTimeHours,
            @RequestParam double resolutionTimeHours,
            RedirectAttributes redirectAttributes) {
        try {
            var dto = SlaPolicyUpdate.builder()
                    .responseTimeHours(responseTimeHours)
                    .resolutionTimeHours(resolutionTimeHours)
                    .build();
            slaPolicyService.update(id, dto);
            redirectAttributes.addFlashAttribute("success", "SLA policy updated successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/sla-policies";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            slaPolicyService.delete(id);
            redirectAttributes.addFlashAttribute("success", "SLA policy deleted.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/sla-policies";
    }
}
