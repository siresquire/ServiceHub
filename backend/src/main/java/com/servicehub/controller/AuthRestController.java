package com.servicehub.controller;

import com.servicehub.dto.*;
import com.servicehub.service.AuthService;
import com.servicehub.service.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and logout")
public class AuthRestController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;

    @Operation(
            summary     = "Register a new user",
            description = "Creates a new user account with role USER. " +
                    "A valid departmentId is required. " +
                    "Returns a JWT token and sets an HttpOnly cookie on success."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully — JWT token returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request body — validation failed or email already exists"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {

        AuthResponse auth = authService.register(request);

        Cookie cookie = new Cookie("token", auth.getToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);

        return ResponseEntity.ok(auth);
    }

    @Operation(
            summary     = "Login",
            description = "Authenticates a user with email and password. " +
                    "Returns a JWT token and sets an HttpOnly cookie. " +
                    "Use the token in the Authorization header as: Bearer <token>."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful — JWT token returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request body — validation failed"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletResponse response) {

        AuthResponse auth = authService.login(request);

        Cookie cookie = new Cookie("token", auth.getToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);

        return ResponseEntity.ok(auth);
    }

    @Operation(
            summary     = "Logout",
            description = "Invalidates the current JWT token by adding it to the blacklist. " +
                    "Clears the HttpOnly cookie. " +
                    "Accepts token from either Authorization header or cookie."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged out successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — no valid token provided")
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        // Blacklist from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tokenBlacklistService.blacklist(authHeader.substring(7));
        }

        // Blacklist from cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("token".equals(c.getName())) {
                    tokenBlacklistService.blacklist(c.getValue());
                }
            }
        }

        // Expire cookie
        Cookie expired = new Cookie("token", null);
        expired.setHttpOnly(true);
        expired.setPath("/");
        expired.setMaxAge(0);
        response.addCookie(expired);

        return ResponseEntity.ok("Logged out successfully");
    }
}