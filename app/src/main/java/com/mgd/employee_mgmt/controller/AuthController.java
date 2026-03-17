package com.mgd.employee_mgmt.controller;

import com.mgd.employee_mgmt.model.User;
import com.mgd.employee_mgmt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        userService.register(body.get("username"), body.get("password"));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Account created successfully.");
        return ResponseEntity.ok(response);
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        User user = userService.login(body.get("username"), body.get("password"));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        return ResponseEntity.ok(response);
    }
}