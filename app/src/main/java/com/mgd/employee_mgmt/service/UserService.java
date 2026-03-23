package com.mgd.employee_mgmt.service;

import com.mgd.employee_mgmt.exception.InvalidCredentialsException;
import com.mgd.employee_mgmt.model.User;
import com.mgd.employee_mgmt.repository.UserRepository;
import com.mgd.employee_mgmt.util.MessageUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageUtil     msg;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       MessageUtil msg) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.msg             = msg;
    }

    // ── Public registration (signup page) ────────────────────

    public void register(String username, String password) {
        validateCredentials(username, password);

        User user = new User(username.trim(), passwordEncoder.encode(password), "ROLE_USER");
        userRepository.save(user);
    }

    // ── Admin registration (users page) ──────────────────────

    public void adminRegister(String username, String password, String role) {
        validateCredentials(username, password);

        String resolvedRole = ("ROLE_ADMIN".equalsIgnoreCase(role)) ? "ROLE_ADMIN" : "ROLE_USER";
        User user = new User(username.trim(), passwordEncoder.encode(password), resolvedRole);
        userRepository.save(user);
    }

    // ── Login ─────────────────────────────────────────────────

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException(
                        msg.get("auth.credentials.invalid")));

        if (!user.isActive())
            throw new InvalidCredentialsException(msg.get("auth.account.inactive"));

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new InvalidCredentialsException(msg.get("auth.credentials.invalid"));

        return user;
    }

    // ── Get all users (admin Users page) ─────────────────────

    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByUsernameAsc();
    }

    // ── Promote ROLE_USER → ROLE_ADMIN ───────────────────────

    public User promoteUser(Long id) {
        User user = findById(id);

        if ("ROLE_ADMIN".equals(user.getRole()))
            throw new IllegalStateException(
                    msg.get("user.already.admin", user.getUsername()));

        user.setRole("ROLE_ADMIN");
        return userRepository.save(user);
    }

    // ── Demote ROLE_ADMIN → ROLE_USER ────────────────────────

    public User demoteUser(Long id) {
        User user = findById(id);

        if (!"ROLE_ADMIN".equals(user.getRole()))
            throw new IllegalStateException(
                    msg.get("user.already.user", user.getUsername()));

        // Block if this is the only active admin
        long activeAdminCount = userRepository.countByRoleAndActiveTrue("ROLE_ADMIN");
        if (activeAdminCount <= 1)
            throw new IllegalStateException(msg.get("user.last.admin.error"));

        user.setRole("ROLE_USER");
        return userRepository.save(user);
    }

    // ── Activate ──────────────────────────────────────────────

    public User activateUser(Long id) {
        User user = findById(id);

        if (user.isActive())
            throw new IllegalStateException(
                    msg.get("user.already.active", user.getUsername()));

        user.setActive(true);
        return userRepository.save(user);
    }

    // ── Deactivate ────────────────────────────────────────────

    public User deactivateUser(Long id) {
        User user = findById(id);

        if (!user.isActive())
            throw new IllegalStateException(
                    msg.get("user.already.inactive", user.getUsername()));

        // Block if this would remove the last active admin
        if ("ROLE_ADMIN".equals(user.getRole())) {
            long activeAdminCount = userRepository.countByRoleAndActiveTrue("ROLE_ADMIN");
            if (activeAdminCount <= 1)
                throw new IllegalStateException(msg.get("user.last.admin.error"));
        }

        user.setActive(false);
        return userRepository.save(user);
    }

    // ── Private helpers ───────────────────────────────────────

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        msg.get("user.not.found.id", id)));
    }

    private void validateCredentials(String username, String password) {
        if (username == null || username.trim().isEmpty())
            throw new IllegalArgumentException(msg.get("auth.username.required"));

        if (password == null || password.trim().isEmpty())
            throw new IllegalArgumentException(msg.get("auth.password.required"));

        if (password.length() < 6)
            throw new IllegalArgumentException(msg.get("auth.password.too.short"));

        if (userRepository.existsByUsername(username.trim()))
            throw new IllegalArgumentException(msg.get("auth.username.taken"));
    }
}
