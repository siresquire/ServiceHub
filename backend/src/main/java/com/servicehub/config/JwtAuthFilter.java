package com.servicehub.config;

import com.servicehub.model.User;
import com.servicehub.repository.UserRepository;
import com.servicehub.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${jwt.secret}")
    private String jwtSecret;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        //  Get the Authorization header
        String authHeader = request.getHeader("Authorization");

        //  If no token, just continue — security config will handle it
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            //  Extract the token (remove "Bearer " prefix)
            String token = authHeader.substring(7);
            if (token != null && SecurityContextHolder.getContext().getAuthentication() ==null){

                //  Validate the token
                if (!jwtService.isTokenValid(token)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // After isTokenValid() check, add:
                if (tokenBlacklistService.isBlacklisted(token)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                //  Extract email from token
                String email = jwtService.extractEmail(token);

                //  Load the user from database
                User user = userRepository.findByEmail(email)
                        .orElse(null);

                if (user == null) {
                    filterChain.doFilter(request, response);
                    return;
                }

                //  Set authentication in Spring Security context
                var auth = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}
