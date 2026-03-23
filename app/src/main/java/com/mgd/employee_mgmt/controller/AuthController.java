package com.mgd.employee_mgmt.controller;

import com.mgd.employee_mgmt.model.User;
import com.mgd.employee_mgmt.service.UserService;
import com.mgd.employee_mgmt.util.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.auth.base}")
@CrossOrigin(origins = "${cors.allowed.origins}")
@PropertySource("classpath:urls.properties")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageUtil msg;

    // POST /api/auth/register
    @PostMapping("${api.auth.register}")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        userService.register(body.get("username"), body.get("password"));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", msg.get("auth.register.success"));
        return ResponseEntity.ok(response);
    }

    // POST /api/auth/login
    @PostMapping("${api.auth.login}")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        User user = userService.login(body.get("username"), body.get("password"));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        return ResponseEntity.ok(response);
    }
}
