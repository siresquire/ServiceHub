package com.servicehub.controller;

import com.servicehub.model.User;
import com.servicehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String view(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        return "profile/view";
    }

    @PostMapping("/update")
    public String update(
            @AuthenticationPrincipal User user,
            @RequestParam String fullName,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            RedirectAttributes redirectAttributes) {
        try {
            User entity = userRepository.findById(user.getId()).orElseThrow();
            entity.setFullName(fullName);

            if (newPassword != null && !newPassword.isBlank()) {
                if (currentPassword == null || currentPassword.isBlank()) {
                    redirectAttributes.addFlashAttribute("error", "Current password is required to change password.");
                    return "redirect:/profile";
                }
                if (!passwordEncoder.matches(currentPassword, entity.getPassword())) {
                    redirectAttributes.addFlashAttribute("error", "Current password is incorrect.");
                    return "redirect:/profile";
                }
                if (newPassword.length() < 6) {
                    redirectAttributes.addFlashAttribute("error", "New password must be at least 6 characters.");
                    return "redirect:/profile";
                }
                entity.setPassword(passwordEncoder.encode(newPassword));
            }

            userRepository.save(entity);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/profile";
    }
}
