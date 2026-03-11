package com.servicehub.init;

import com.servicehub.config.DefaultAccountsProperties;
import com.servicehub.model.Department;
import com.servicehub.model.User;
import com.servicehub.model.enums.RequestCategory;
import com.servicehub.model.enums.Role;
import com.servicehub.repository.DepartmentRepository;
import com.servicehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final DepartmentRepository departmentRepository;
  private final PasswordEncoder passwordEncoder;
  private final DefaultAccountsProperties  defaultAccountsProperties;
  private static final Logger logger = LoggerFactory.getLogger(UserInitializer.class);


  @Override
  public void run(String... args) throws Exception {
    // If no departments exist, create a default IT Support department
    logger.info("Default user initializer running...");
      Department defaultDepartment = departmentRepository.findByCategory(RequestCategory.IT_SUPPORT)
              .orElseGet(() -> {
                        Department dept = Department.builder()
                                .name("IT Support")
                                .category(RequestCategory.IT_SUPPORT)
                                .description("Handles all IT related support requests, including hardware, software, and network issues.")
                                .build();
                        return departmentRepository.save(dept);
                      }
              );

    // If no users exist, create default admin, user, and agent accounts from properties
    if(userRepository.count() == 0) {
      logger.info("No users found. Creating default admin, user, and agent accounts...");
      userRepository.saveAll(List.of(
              toUser(defaultAccountsProperties.getAdmin(), Role.ADMIN, defaultDepartment),
              toUser(defaultAccountsProperties.getUser(), Role.USER, defaultDepartment),
              toUser(defaultAccountsProperties.getAgent(), Role.AGENT, defaultDepartment)
      ));
    }
    logger.info("Default user initializer completed.");
  }

  private User toUser(DefaultAccountsProperties.Account account, Role role, Department department) {
    return User.builder()
            .email(account.getEmail())
            .password(passwordEncoder.encode(account.getPassword()))
            .fullName("Default " + role.name())
            .department(department) // set later
            .role(role)
            .build();
  }
}
