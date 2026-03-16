package com.mgd.employee_mgmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mgd.employee_mgmt.model.Employee;
import com.mgd.employee_mgmt.security.SecurityConfig;
import com.mgd.employee_mgmt.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@Import(SecurityConfig.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private ObjectMapper objectMapper;
    private Employee sampleEmployee;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        sampleEmployee = new Employee(
                "EMP001",
                "John Doe",
                LocalDate.of(1990, 5, 15),
                "Engineering",
                75000.0
        );
        sampleEmployee.setId(1L);
    }

    // ─── POST /api/employees ──────────────────────────────────────────────────

    @Test
    void createEmployee_returns201() throws Exception {
        when(employeeService.saveEmployee(any(Employee.class))).thenReturn(sampleEmployee);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEmployee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeId", is("EMP001")))
                .andExpect(jsonPath("$.name", is("John Doe")));
    }

    @Test
    void createEmployee_throwsWhenServiceThrows() throws Exception {
        when(employeeService.saveEmployee(any(Employee.class)))
                .thenThrow(new RuntimeException("Employee with ID EMP001 already exists"));

        assertThrows(Exception.class, () ->
                mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEmployee))));
    }

    // ─── GET /api/employees ───────────────────────────────────────────────────

    @Test
    void getAllEmployees_returns200WithList() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(sampleEmployee));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].employeeId", is("EMP001")));
    }

    @Test
    void getAllEmployees_returns200WithEmptyList() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ─── GET /api/employees/{id} ──────────────────────────────────────────────

    @Test
    void getEmployeeById_returns200() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(sampleEmployee);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId", is("EMP001")));
    }

    @Test
    void getEmployeeById_throwsWhenNotFound() throws Exception {
        when(employeeService.getEmployeeById(99L))
                .thenThrow(new RuntimeException("Employee not found with id: 99"));

        assertThrows(Exception.class, () ->
                mockMvc.perform(get("/api/employees/99")));
    }

    // ─── GET /api/employees/employee-id/{employeeId} ─────────────────────────

    @Test
    void getEmployeeByEmployeeId_returns200() throws Exception {
        when(employeeService.getEmployeeByEmployeeId("EMP001")).thenReturn(sampleEmployee);

        mockMvc.perform(get("/api/employees/employee-id/EMP001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId", is("EMP001")));
    }

    // ─── PUT /api/employees/{id} ──────────────────────────────────────────────

    @Test
    void updateEmployee_returns200() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(Employee.class))).thenReturn(sampleEmployee);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEmployee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId", is("EMP001")));
    }

    @Test
    void updateEmployee_throwsWhenNotFound() throws Exception {
        when(employeeService.updateEmployee(eq(99L), any(Employee.class)))
                .thenThrow(new RuntimeException("Employee not found with id: 99"));

        assertThrows(Exception.class, () ->
                mockMvc.perform(put("/api/employees/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEmployee))));
    }

    // ─── DELETE /api/employees/{id} ───────────────────────────────────────────

    @Test
    void deleteEmployee_returns200() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Employee deleted successfully")));
    }

    @Test
    void deleteEmployee_throwsWhenNotFound() throws Exception {
        doThrow(new RuntimeException("Employee not found with id: 99"))
                .when(employeeService).deleteEmployee(99L);

        assertThrows(Exception.class, () ->
                mockMvc.perform(delete("/api/employees/99")));
    }

    // ─── GET /api/employees/search ────────────────────────────────────────────

    @Test
    void searchEmployees_returns200() throws Exception {
        when(employeeService.searchEmployeesByName("John")).thenReturn(List.of(sampleEmployee));

        mockMvc.perform(get("/api/employees/search").param("name", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("John Doe")));
    }

    // ─── GET /api/employees/department/{department} ───────────────────────────

    @Test
    void getEmployeesByDepartment_returns200() throws Exception {
        when(employeeService.getEmployeesByDepartment("Engineering"))
                .thenReturn(List.of(sampleEmployee));

        mockMvc.perform(get("/api/employees/department/Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ─── GET /api/employees/statistics/average-salary ────────────────────────

    @Test
    void getAverageSalary_returns200() throws Exception {
        when(employeeService.calculateAverageSalary()).thenReturn(75000.0);

        mockMvc.perform(get("/api/employees/statistics/average-salary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary", is(75000.0)));
    }

    // ─── GET /api/employees/statistics/average-age ───────────────────────────

    @Test
    void getAverageAge_returns200() throws Exception {
        when(employeeService.calculateAverageAge()).thenReturn(34.0);

        mockMvc.perform(get("/api/employees/statistics/average-age"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageAge", is(34.0)));
    }

    // ─── GET /api/employees/reports/by-department ────────────────────────────

    @Test
    void getEmployeesByDepartmentReport_returns200() throws Exception {
        when(employeeService.getEmployeesOrderedByDepartment()).thenReturn(List.of(sampleEmployee));

        mockMvc.perform(get("/api/employees/reports/by-department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ─── GET /api/employees/reports/by-age ───────────────────────────────────

    @Test
    void getEmployeesByAgeReport_returns200() throws Exception {
        when(employeeService.getEmployeesOrderedByAge()).thenReturn(List.of(sampleEmployee));

        mockMvc.perform(get("/api/employees/reports/by-age"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}