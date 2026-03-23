package com.mgd.employee_mgmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgd.employee_mgmt.config.AppConfig;
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

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, AppConfig.class, MessageUtil.class})
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean  private UserService userService;

    private ObjectMapper objectMapper;
    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        adminUser = new User("admin", "hashed", "ROLE_ADMIN");
        adminUser.setId(1L);

        regularUser = new User("alice", "hashed", "ROLE_USER");
        regularUser.setId(2L);
    }

    // ── GET /api/users ────────────────────────────────────────────────────────

    @Test
    void getAllUsers_returns200WithList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(adminUser, regularUser));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("admin")))
                .andExpect(jsonPath("$[0].role", is("ROLE_ADMIN")))
                .andExpect(jsonPath("$[0].active", is(true)));
    }

    @Test
    void getAllUsers_doesNotExposePasswords() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(regularUser));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void getAllUsers_returns200WithEmptyList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── POST /api/users ───────────────────────────────────────────────────────

    @Test
    void registerUser_returns201() throws Exception {
        doNothing().when(userService).adminRegister("bob", "secret123", "ROLE_USER");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "bob", "password", "secret123", "role", "ROLE_USER"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User registered successfully.")));
    }

    @Test
    void registerUser_returns400WhenUsernameTaken() throws Exception {
        doThrow(new IllegalArgumentException("Username already taken. Please choose another."))
                .when(userService).adminRegister("admin", "secret123", "ROLE_USER");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "admin", "password", "secret123", "role", "ROLE_USER"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already taken")));
    }

    @Test
    void registerUser_returns400WhenPasswordTooShort() throws Exception {
        doThrow(new IllegalArgumentException("Password must be at least 6 characters."))
                .when(userService).adminRegister("bob", "abc", "ROLE_USER");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "bob", "password", "abc", "role", "ROLE_USER"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("at least 6 characters")));
    }

    // ── PATCH /api/users/{id}/role ────────────────────────────────────────────

    @Test
    void promoteUser_returns200() throws Exception {
        regularUser.setRole("ROLE_ADMIN");
        when(userService.promoteUser(2L)).thenReturn(regularUser);

        mockMvc.perform(patch("/api/users/2/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "promote"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("promoted")));
    }

    @Test
    void demoteUser_returns200() throws Exception {
        adminUser.setRole("ROLE_USER");
        when(userService.demoteUser(1L)).thenReturn(adminUser);

        mockMvc.perform(patch("/api/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "demote"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("demoted")));
    }

    @Test
    void demoteUser_returns409WhenLastAdmin() throws Exception {
        when(userService.demoteUser(1L))
                .thenThrow(new IllegalStateException("Should have at least one admin."));

        mockMvc.perform(patch("/api/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "demote"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("at least one admin")));
    }

    @Test
    void updateRole_returns404WhenUserNotFound() throws Exception {
        when(userService.promoteUser(99L))
                .thenThrow(new NoSuchElementException("User not found with id: 99"));

        mockMvc.perform(patch("/api/users/99/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "promote"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void updateRole_returns400WhenActionInvalid() throws Exception {
        mockMvc.perform(patch("/api/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "invalid"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("promote")));
    }

    // ── PATCH /api/users/{id}/status ──────────────────────────────────────────

    @Test
    void activateUser_returns200() throws Exception {
        regularUser.setActive(true);
        when(userService.activateUser(2L)).thenReturn(regularUser);

        mockMvc.perform(patch("/api/users/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "activate"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("activated")));
    }

    @Test
    void deactivateUser_returns200() throws Exception {
        regularUser.setActive(false);
        when(userService.deactivateUser(2L)).thenReturn(regularUser);

        mockMvc.perform(patch("/api/users/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "deactivate"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("deactivated")));
    }

    @Test
    void deactivateUser_returns409WhenLastActiveAdmin() throws Exception {
        when(userService.deactivateUser(1L))
                .thenThrow(new IllegalStateException("Should have at least one admin."));

        mockMvc.perform(patch("/api/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "deactivate"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("at least one admin")));
    }

    @Test
    void updateStatus_returns404WhenUserNotFound() throws Exception {
        when(userService.activateUser(99L))
                .thenThrow(new NoSuchElementException("User not found with id: 99"));

        mockMvc.perform(patch("/api/users/99/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "activate"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void updateStatus_returns400WhenActionInvalid() throws Exception {
        mockMvc.perform(patch("/api/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "invalid"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("activate")));
    }
}
