package com.servicehub.service;

import com.servicehub.config.JwtService;
import com.servicehub.dto.*;
import com.servicehub.exception.InvalidCredentialsException;
import com.servicehub.exception.NotFoundException;
import com.servicehub.model.Department;
import com.servicehub.model.User;
import com.servicehub.model.enums.Role;
import com.servicehub.repository.DepartmentRepository;
import com.servicehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;  // ← inject this, not jwt.secret

    public AuthResponse register(RegisterRequest request) {
        // 1. Check email doesn't already exist
        if (userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already exist. try again");
        }
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(()->new NotFoundException("Department not found with id: " + request.getDepartmentId()));

        // 2. Build and save User (encode password!)
        User user = User.builder()
                .fullName(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(Role.USER)
                .department(department)
                .build();

        userRepository.save(user);

        // 3. Generate token via jwtService

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        // 4. Return AuthResponse with token, email, role, fullName

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .build();
    }

    public AuthResponse login(AuthRequest request) {

        // 1. Find user by email or throw RuntimeException("User not found")
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()->new InvalidCredentialsException("user not found"));

        // 2. Check password matches or throw RuntimeException("Invalid credentials")
        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // 3. Generate token via jwtService
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        // 4. Return AuthResponse
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .build();
    }
}
