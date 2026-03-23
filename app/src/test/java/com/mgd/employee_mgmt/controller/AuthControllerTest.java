package com.mgd.employee_mgmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgd.employee_mgmt.config.AppConfig;
import com.mgd.employee_mgmt.exception.InvalidCredentialsException;
import com.mgd.employee_mgmt.model.User;
import com.mgd.employee_mgmt.security.SecurityConfig;
import com.mgd.employee_mgmt.service.UserService;
import com.mgd.employee_mgmt.util.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AppConfig.class, MessageUtil.class})
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean  private UserService userService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    // ─── POST /api/auth/register ──────────────────────────────────────────────

    @Test
    void register_success() throws Exception {
        doNothing().when(userService).register("alice", "secret");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "alice", "password", "secret"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Account created successfully.")));
    }

    @Test
    void register_failsWhenUsernameBlank() throws Exception {
        doThrow(new IllegalArgumentException("Username is required."))
                .when(userService).register("  ", "secret");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "  ", "password", "secret"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Username is required.")));
    }

    @Test
    void register_failsWhenPasswordBlank() throws Exception {
        doThrow(new IllegalArgumentException("Password is required."))
                .when(userService).register("alice", "");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "alice", "password", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Password is required.")));
    }

    @Test
    void register_failsWhenPasswordTooShort() throws Exception {
        doThrow(new IllegalArgumentException("Password must be at least 6 characters."))
                .when(userService).register("alice", "abc");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "alice", "password", "abc"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Password must be at least 6 characters.")));
    }

    @Test
    void register_failsWhenUsernameTaken() throws Exception {
        doThrow(new IllegalArgumentException("Username already taken. Please choose another."))
                .when(userService).register("alice", "secret");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "alice", "password", "secret"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Username already taken. Please choose another.")));
    }

    @Test
    void register_failsWhenUsernameMissing() throws Exception {
        doThrow(new IllegalArgumentException("Username is required."))
                .when(userService).register(null, "secret");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("password", "secret"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Username is required.")));
    }

    // ─── POST /api/auth/login ─────────────────────────────────────────────────

    @Test
    void login_success() throws Exception {
        User user = new User("alice", "hashed", "ROLE_USER");
        when(userService.login("alice", "secret")).thenReturn(user);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "alice", "password", "secret"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.username", is("alice")))
                .andExpect(jsonPath("$.role", is("ROLE_USER")));
    }

    @Test
    void login_failsWhenUserNotFound() throws Exception {
        when(userService.login("unknown", "pass"))
                .thenThrow(new InvalidCredentialsException("Invalid username or password."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "unknown", "password", "pass"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Invalid username or password.")));
    }

    @Test
    void login_failsWhenPasswordWrong() throws Exception {
        when(userService.login("alice", "wrong"))
                .thenThrow(new InvalidCredentialsException("Invalid username or password."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "alice", "password", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Invalid username or password.")));
    }
}
