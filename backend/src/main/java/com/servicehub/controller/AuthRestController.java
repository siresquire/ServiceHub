package com.servicehub.controller;

import com.servicehub.dto.*;
import com.servicehub.service.AuthService;
import com.servicehub.service.TokenBlacklistService;
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
public class AuthRestController {
  private final AuthService authService;
  private final TokenBlacklistService tokenBlacklistService;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                               HttpServletResponse response) {
    AuthResponse auth = authService.register(request);

    Cookie cookie = new Cookie("token", auth.getToken());
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(86400);
    response.addCookie(cookie);

    return ResponseEntity.ok(auth);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request,
                                            HttpServletResponse response) {
    AuthResponse auth = authService.login(request);

    // Set HttpOnly cookie
    Cookie cookie = new Cookie("token", auth.getToken());
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(86400); // 1 day
    response.addCookie(cookie);

    return ResponseEntity.ok(auth);
  }

  @PostMapping("/logout")
  public ResponseEntity<String> logout(HttpServletRequest request,
                                       HttpServletResponse response) {
    // Blacklist the token
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      tokenBlacklistService.blacklist(authHeader.substring(7));
    }

    // Also check cookie
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie c : cookies) {
        if (c.getName().equals("token")) {
          tokenBlacklistService.blacklist(c.getValue());
        }
      }
    }

    // Delete cookie
    Cookie cookie = new Cookie("token", null);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);

    return ResponseEntity.ok("Logged out successfully");
  }
}
