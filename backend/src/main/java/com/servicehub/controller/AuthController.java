package com.servicehub.controller;

import com.servicehub.dto.AuthRequest;
import com.servicehub.dto.AuthResponse;
import com.servicehub.dto.RegisterRequest;
import com.servicehub.service.AuthService;
import com.servicehub.service.TokenBlacklistService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterRequest registerRequest,
                           HttpServletResponse response,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        try {
            AuthResponse auth = authService.register(registerRequest);

            Cookie cookie = new Cookie("token", auth.getToken());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(86400);
            response.addCookie(cookie);

            redirectAttributes.addFlashAttribute("success", "Account created! Please sign in.");
            return "redirect:/auth/login";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("registerRequest", registerRequest);
            return "auth/register";
        }
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpServletResponse response,
                        Model model) {
        try {
            AuthRequest request = new AuthRequest();
            request.setEmail(email);
            request.setPassword(password);

            AuthResponse auth = authService.login(request);

            Cookie cookie = new Cookie("token", auth.getToken());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(86400);
            response.addCookie(cookie);

            // ← redirect based on role
            return switch (auth.getRole()) {
                case "ADMIN" -> "redirect:/admin/dashboard";
                case "AGENT" -> "redirect:/agent/dashboard";
                default      -> "redirect:/user/dashboard";
            };

        } catch (RuntimeException ex) {
            log.info("Login error: {}", ex);
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("lastEmail", email);
            return "auth/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("token".equals(c.getName())) {
                    tokenBlacklistService.blacklist(c.getValue());
                }
            }
        }
        Cookie expired = new Cookie("token", null);
        expired.setHttpOnly(true);
        expired.setPath("/");
        expired.setMaxAge(0);
        response.addCookie(expired);
        return "redirect:/auth/login";
    }
}
