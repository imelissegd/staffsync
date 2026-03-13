package com.mgd.employee_mgmt.controller;

import com.mgd.employee_mgmt.model.User;
import com.mgd.employee_mgmt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();

        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Username is required.");
            return ResponseEntity.badRequest().body(response);
        }

        if (password == null || password.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Password is required.");
            return ResponseEntity.badRequest().body(response);
        }

        if (userRepository.existsByUsername(username.trim())) {
            response.put("success", false);
            response.put("message", "Username already taken. Please choose another.");
            return ResponseEntity.badRequest().body(response);
        }

        User user = new User(
                username.trim(),
                passwordEncoder.encode(password),
                "ROLE_USER"
        );
        userRepository.save(user);

        response.put("success", true);
        response.put("message", "Account created successfully.");
        return ResponseEntity.ok(response);
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();

        String username = body.get("username");
        String password = body.get("password");

        Optional<User> optUser = userRepository.findByUsername(username);

        if (optUser.isEmpty() || !passwordEncoder.matches(password, optUser.get().getPassword())) {
            response.put("success", false);
            response.put("message", "Invalid username or password.");
            return ResponseEntity.status(401).body(response);
        }

        User user = optUser.get();
        response.put("success", true);
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        return ResponseEntity.ok(response);
    }
}