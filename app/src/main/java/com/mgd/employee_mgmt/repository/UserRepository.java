package com.mgd.employee_mgmt.repository;

import com.mgd.employee_mgmt.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    // Count how many active admins exist — used for demotion/deactivation guard
    long countByRoleAndActiveTrue(String role);

    // List all users for the admin Users page
    List<User> findAllByOrderByUsernameAsc();
}
