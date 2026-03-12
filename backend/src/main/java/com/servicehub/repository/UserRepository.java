package com.servicehub.repository;

import com.servicehub.model.User;
import com.servicehub.model.enums.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    // Eagerly fetch department to avoid LazyInitializationException in Thymeleaf templates
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department WHERE u.email = :email")
    Optional<User> findByEmailWithDepartment(@Param("email") String email);
}
