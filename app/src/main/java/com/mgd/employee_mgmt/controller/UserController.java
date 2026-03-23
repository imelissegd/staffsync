package com.mgd.employee_mgmt.controller;

import com.mgd.employee_mgmt.model.User;
import com.mgd.employee_mgmt.service.UserService;
import com.mgd.employee_mgmt.util.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.users.base}")
@CrossOrigin(origins = "${cors.allowed.origins}")
@PropertySource("classpath:urls.properties")
public class UserController {

    private final UserService userService;
    private final MessageUtil msg;

    @Autowired
    public UserController(UserService userService, MessageUtil msg) {
        this.userService = userService;
        this.msg         = msg;
    }

    // GET /api/users — list all users (no passwords)
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = userService.getAllUsers()
                .stream()
                .map(this::toSafeMap)
                .toList();
        return ResponseEntity.ok(users);
    }

    // POST /api/users — admin registers a new user
    @PostMapping
    public ResponseEntity<Map<String, Object>> registerUser(
            @RequestBody Map<String, String> body) {
        userService.adminRegister(
                body.get("username"),
                body.get("password"),
                body.get("role"));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", msg.get("user.registered.success"));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // PATCH /api/users/{id}/role — promote or demote
    @PatchMapping("/{id}/role")
    public ResponseEntity<Map<String, Object>> updateRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String action = body.get("action"); // "promote" or "demote"
        User updated;

        if ("promote".equalsIgnoreCase(action)) {
            updated = userService.promoteUser(id);
            return buildResponse(true,
                    msg.get("user.promoted.success", updated.getUsername()),
                    updated);
        } else if ("demote".equalsIgnoreCase(action)) {
            updated = userService.demoteUser(id);
            return buildResponse(true,
                    msg.get("user.demoted.success", updated.getUsername()),
                    updated);
        } else {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Invalid action. Use 'promote' or 'demote'."));
        }
    }

    // PATCH /api/users/{id}/status — activate or deactivate
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String action = body.get("action"); // "activate" or "deactivate"
        User updated;

        if ("activate".equalsIgnoreCase(action)) {
            updated = userService.activateUser(id);
            return buildResponse(true,
                    msg.get("user.activated.success", updated.getUsername()),
                    updated);
        } else if ("deactivate".equalsIgnoreCase(action)) {
            updated = userService.deactivateUser(id);
            return buildResponse(true,
                    msg.get("user.deactivated.success", updated.getUsername()),
                    updated);
        } else {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Invalid action. Use 'activate' or 'deactivate'."));
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private Map<String, Object> toSafeMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id",       user.getId());
        map.put("username", user.getUsername());
        map.put("role",     user.getRole());
        map.put("active",   user.isActive());
        return map;
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            boolean success, String message, User user) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", success);
        body.put("message", message);
        body.put("user",    toSafeMap(user));
        return ResponseEntity.ok(body);
    }
}
