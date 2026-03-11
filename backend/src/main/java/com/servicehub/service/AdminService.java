package com.servicehub.service;

import com.servicehub.dto.CreateDepartmentRequest;
import com.servicehub.dto.UserResponse;
import com.servicehub.exception.NotFoundException;
import com.servicehub.model.Department;
import com.servicehub.model.User;
import com.servicehub.model.enums.Role;
import com.servicehub.repository.DepartmentRepository;
import com.servicehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public UserResponse updateUserRole(Long userId, Role role, Long departmentId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Cannot change another admin's role
        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Cannot change role of another ADMIN");
        }

        // Department required when promoting to AGENT
        if (role == Role.AGENT) {
            if (departmentId == null) {
                throw new RuntimeException("Department is required when assigning AGENT role");
            }
            Department dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new NotFoundException("Department not found"));
            user.setDepartment(dept);
        }

        user.setRole(role);
        userRepository.save(user);

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .department(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .build();
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> UserResponse.builder()
                        .id(u.getId())
                        .fullName(u.getFullName())
                        .email(u.getEmail())
                        .role(u.getRole().name())
                        .department(u.getDepartment() != null ? u.getDepartment().getName() : null)
                        .build())
                .toList();
    }
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        return UserResponse.from(user);
    }

    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(UserResponse::from)
                .toList();
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Cannot delete an ADMIN user");
        }
        userRepository.delete(user);
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }


    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found with id: " + id));
    }

    public Department createDepartment(CreateDepartmentRequest request) {
        if (departmentRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Department '" + request.getName() + "' already exists");
        }
        Department dept = Department.builder()
                .name(request.getName())
                .category(request.getCategory())
                .build();
        return departmentRepository.save(dept);
    }

    public Department updateDepartment(Long id, CreateDepartmentRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found with id: " + id));
        dept.setName(request.getName());
        dept.setCategory(request.getCategory());

        return departmentRepository.save(dept);
    }

    public Department toggleDepartmentActive(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found with id: " + id));
        return departmentRepository.save(dept);
    }

    public void deleteDepartment(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found with id: " + id));
        departmentRepository.delete(dept);
    }
}