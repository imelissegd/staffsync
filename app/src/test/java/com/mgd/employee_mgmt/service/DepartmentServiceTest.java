package com.mgd.employee_mgmt.service;

import com.mgd.employee_mgmt.model.Department;
import com.mgd.employee_mgmt.repository.DepartmentRepository;
import com.mgd.employee_mgmt.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock private DepartmentRepository departmentRepository;
    @Mock private EmployeeRepository   employeeRepository;

    @InjectMocks private DepartmentService departmentService;

    private Department sampleDept;

    @BeforeEach
    void setUp() {
        sampleDept = new Department("Engineering", "Software engineers");
        sampleDept.setId(1L);
    }

    // ── createDepartment ──────────────────────────────────────────────────────

    @Test
    void createDepartment_success() {
        when(departmentRepository.existsByName("Engineering")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(sampleDept);

        Department result = departmentService.createDepartment(sampleDept);

        assertNotNull(result);
        assertEquals("Engineering", result.getName());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void createDepartment_throwsWhenNameBlank() {
        Department blank = new Department("  ", null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> departmentService.createDepartment(blank));

        assertTrue(ex.getMessage().contains("Department name is required"));
        verify(departmentRepository, never()).save(any());
    }

    @Test
    void createDepartment_throwsWhenNameNull() {
        Department nullName = new Department(null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> departmentService.createDepartment(nullName));

        assertTrue(ex.getMessage().contains("Department name is required"));
    }

    @Test
    void createDepartment_throwsWhenDuplicateName() {
        when(departmentRepository.existsByName("Engineering")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> departmentService.createDepartment(sampleDept));

        assertTrue(ex.getMessage().contains("already exists"));
        verify(departmentRepository, never()).save(any());
    }

    // ── getAllDepartments ──────────────────────────────────────────────────────

    @Test
    void getAllDepartments_returnsSortedList() {
        Department hr = new Department("HR", "Human resources");
        hr.setId(2L);
        when(departmentRepository.findAll()).thenReturn(List.of(sampleDept, hr));

        List<Department> result = departmentService.getAllDepartments();

        assertEquals(2, result.size());
        // Should be sorted alphabetically: Engineering, HR
        assertEquals("Engineering", result.get(0).getName());
        assertEquals("HR", result.get(1).getName());
    }

    @Test
    void getAllDepartments_returnsEmptyList() {
        when(departmentRepository.findAll()).thenReturn(List.of());

        List<Department> result = departmentService.getAllDepartments();

        assertTrue(result.isEmpty());
    }

    // ── getDepartmentById ──────────────────────────────────────────────────────

    @Test
    void getDepartmentById_success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDept));

        Department result = departmentService.getDepartmentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getDepartmentById_throwsWhenNotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> departmentService.getDepartmentById(99L));

        assertTrue(ex.getMessage().contains("Department not found with id: 99"));
    }

    // ── getDepartmentByName ────────────────────────────────────────────────────

    @Test
    void getDepartmentByName_success() {
        when(departmentRepository.findByName("Engineering")).thenReturn(Optional.of(sampleDept));

        Department result = departmentService.getDepartmentByName("Engineering");

        assertNotNull(result);
        assertEquals("Engineering", result.getName());
    }

    @Test
    void getDepartmentByName_throwsWhenNotFound() {
        when(departmentRepository.findByName("Finance")).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> departmentService.getDepartmentByName("Finance"));

        assertTrue(ex.getMessage().contains("Department not found with name: Finance"));
    }

    // ── updateDepartment ──────────────────────────────────────────────────────

    @Test
    void updateDepartment_success() {
        Department updated = new Department("Engineering Updated", "Updated description");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDept));
        when(departmentRepository.existsByName("Engineering Updated")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(sampleDept);

        Department result = departmentService.updateDepartment(1L, updated);

        assertNotNull(result);
        verify(departmentRepository).save(sampleDept);
        assertEquals("Engineering Updated", sampleDept.getName());
        assertEquals("Updated description", sampleDept.getDescription());
    }

    @Test
    void updateDepartment_sameName_doesNotThrowDuplicateError() {
        // Updating with the same name should be allowed (case-insensitive compare)
        Department same = new Department("Engineering", "New description");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDept));
        when(departmentRepository.save(any(Department.class))).thenReturn(sampleDept);

        assertDoesNotThrow(() -> departmentService.updateDepartment(1L, same));
    }

    @Test
    void updateDepartment_throwsWhenNewNameAlreadyTaken() {
        Department updated = new Department("HR", "Conflict");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDept));
        when(departmentRepository.existsByName("HR")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> departmentService.updateDepartment(1L, updated));

        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void updateDepartment_throwsWhenNotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> departmentService.updateDepartment(99L, sampleDept));
    }

    // ── deleteDepartment ──────────────────────────────────────────────────────

    @Test
    void deleteDepartment_success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDept));
        when(employeeRepository.existsByDepartment(sampleDept)).thenReturn(false);
        doNothing().when(departmentRepository).delete(sampleDept);

        assertDoesNotThrow(() -> departmentService.deleteDepartment(1L));
        verify(departmentRepository).delete(sampleDept);
    }

    @Test
    void deleteDepartment_throwsWhenEmployeesExist() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDept));
        when(employeeRepository.existsByDepartment(sampleDept)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> departmentService.deleteDepartment(1L));

        assertTrue(ex.getMessage().contains("still has employees"));
        verify(departmentRepository, never()).delete(any());
    }

    @Test
    void deleteDepartment_throwsWhenNotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> departmentService.deleteDepartment(99L));

        verify(departmentRepository, never()).delete(any());
    }
}