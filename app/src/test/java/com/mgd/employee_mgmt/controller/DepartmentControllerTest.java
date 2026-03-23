package com.mgd.employee_mgmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgd.employee_mgmt.config.AppConfig;
import com.mgd.employee_mgmt.model.Department;
import com.mgd.employee_mgmt.security.SecurityConfig;
import com.mgd.employee_mgmt.service.DepartmentService;
import com.mgd.employee_mgmt.util.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
@Import({SecurityConfig.class, AppConfig.class, MessageUtil.class})
class DepartmentControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean  private DepartmentService departmentService;

    private ObjectMapper objectMapper;
    private Department   sampleDept;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        sampleDept   = new Department("Engineering", "Software engineers");
        sampleDept.setId(1L);
    }

    // ── POST /api/departments ────────────────────────────────────────────────

    @Test
    void createDepartment_returns201() throws Exception {
        when(departmentService.createDepartment(any(Department.class))).thenReturn(sampleDept);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDept)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Engineering")))
                .andExpect(jsonPath("$.description", is("Software engineers")));
    }

    @Test
    void createDepartment_returns400WhenDuplicate() throws Exception {
        when(departmentService.createDepartment(any(Department.class)))
                .thenThrow(new IllegalArgumentException(
                        "Department with name 'Engineering' already exists."));

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDept)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    void createDepartment_returns400WhenNameBlank() throws Exception {
        when(departmentService.createDepartment(any(Department.class)))
                .thenThrow(new IllegalArgumentException("Department name is required."));

        Department blank = new Department("", null);
        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blank)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("required")));
    }

    // ── GET /api/departments ──────────────────────────────────────────────────

    @Test
    void getAllDepartments_returns200WithList() throws Exception {
        when(departmentService.getAllDepartments()).thenReturn(List.of(sampleDept));

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Engineering")));
    }

    @Test
    void getAllDepartments_returns200WithEmptyList() throws Exception {
        when(departmentService.getAllDepartments()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── GET /api/departments/{id} ─────────────────────────────────────────────

    @Test
    void getDepartmentById_returns200() throws Exception {
        when(departmentService.getDepartmentById(1L)).thenReturn(sampleDept);

        mockMvc.perform(get("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Engineering")));
    }

    @Test
    void getDepartmentById_returns404WhenNotFound() throws Exception {
        when(departmentService.getDepartmentById(99L))
                .thenThrow(new NoSuchElementException("Department not found with id: 99"));

        mockMvc.perform(get("/api/departments/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    // ── GET /api/departments/name/{name} ──────────────────────────────────────

    @Test
    void getDepartmentByName_returns200() throws Exception {
        when(departmentService.getDepartmentByName("Engineering")).thenReturn(sampleDept);

        mockMvc.perform(get("/api/departments/name/Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Engineering")));
    }

    @Test
    void getDepartmentByName_returns404WhenNotFound() throws Exception {
        when(departmentService.getDepartmentByName("Finance"))
                .thenThrow(new NoSuchElementException("Department not found with name: Finance"));

        mockMvc.perform(get("/api/departments/name/Finance"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    // ── PUT /api/departments/{id} ─────────────────────────────────────────────

    @Test
    void updateDepartment_returns200() throws Exception {
        Department updated = new Department("Engineering Updated", "Updated desc");
        updated.setId(1L);
        when(departmentService.updateDepartment(eq(1L), any(Department.class))).thenReturn(updated);

        mockMvc.perform(put("/api/departments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Engineering Updated")));
    }

    @Test
    void updateDepartment_returns404WhenNotFound() throws Exception {
        when(departmentService.updateDepartment(eq(99L), any(Department.class)))
                .thenThrow(new NoSuchElementException("Department not found with id: 99"));

        mockMvc.perform(put("/api/departments/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDept)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void updateDepartment_returns400WhenNameConflict() throws Exception {
        when(departmentService.updateDepartment(eq(1L), any(Department.class)))
                .thenThrow(new IllegalArgumentException("Department with name 'HR' already exists."));

        mockMvc.perform(put("/api/departments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDept)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    // ── DELETE /api/departments/{id} ──────────────────────────────────────────

    @Test
    void deleteDepartment_returns200() throws Exception {
        doNothing().when(departmentService).deleteDepartment(1L);

        mockMvc.perform(delete("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Department deleted successfully.")));
    }

    @Test
    void deleteDepartment_returns404WhenNotFound() throws Exception {
        doThrow(new NoSuchElementException("Department not found with id: 99"))
                .when(departmentService).deleteDepartment(99L);

        mockMvc.perform(delete("/api/departments/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void deleteDepartment_returns409WhenEmployeesExist() throws Exception {
        doThrow(new IllegalStateException(
                "Cannot delete department 'Engineering' because it still has employees assigned to it."))
                .when(departmentService).deleteDepartment(1L);

        mockMvc.perform(delete("/api/departments/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("still has employees")));
    }
}
