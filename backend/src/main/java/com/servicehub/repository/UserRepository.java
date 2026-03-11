package com.servicehub.repository;

import com.servicehub.model.User;
import com.servicehub.model.enums.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"department"})
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    List<User> findByRole(Role role);
}
