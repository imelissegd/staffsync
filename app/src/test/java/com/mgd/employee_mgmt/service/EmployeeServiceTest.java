package com.mgd.employee_mgmt.service;

import com.mgd.employee_mgmt.model.Department;
import com.mgd.employee_mgmt.model.Employee;
import com.mgd.employee_mgmt.repository.DepartmentRepository;
import com.mgd.employee_mgmt.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock private EmployeeRepository   employeeRepository;
    @Mock private DepartmentRepository departmentRepository;

    @InjectMocks private EmployeeService employeeService;

    private Department sampleDept;
    private Employee   sampleEmployee;

    @BeforeEach
    void setUp() {
        sampleDept = new Department("Engineering", "Engineers");
        sampleDept.setId(1L);

        sampleEmployee = new Employee(
                "EMP001", "John Doe",
                LocalDate.of(1990, 5, 15),
                sampleDept, 75000.0
        );
        sampleEmployee.setId(1L);
    }

    // ── saveEmployee ──────────────────────────────────────────────────────────

    @Test
    void saveEmployee_success() {
        when(employeeRepository.existsByEmployeeId("EMP001")).thenReturn(false);
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(employeeRepository.save(sampleEmployee)).thenReturn(sampleEmployee);

        Employee result = employeeService.saveEmployee(sampleEmployee);

        assertNotNull(result);
        assertEquals("EMP001", result.getEmployeeId());
        verify(employeeRepository).save(sampleEmployee);
    }

    @Test
    void saveEmployee_throwsWhenDuplicateEmployeeId() {
        // validateEmployee() runs first and checks departmentRepository — must be stubbed
        // so validation passes and the duplicate-ID check is actually reached
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(employeeRepository.existsByEmployeeId("EMP001")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.saveEmployee(sampleEmployee));

        assertTrue(ex.getMessage().contains("already exists"));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void saveEmployee_throwsWhenEmployeeIdBlank() {
        sampleEmployee.setEmployeeId("  ");

        assertThrows(IllegalArgumentException.class,
                () -> employeeService.saveEmployee(sampleEmployee));
    }

    @Test
    void saveEmployee_throwsWhenNameBlank() {
        sampleEmployee.setName("");

        assertThrows(IllegalArgumentException.class,
                () -> employeeService.saveEmployee(sampleEmployee));
    }

    @Test
    void saveEmployee_throwsWhenDateOfBirthNull() {
        sampleEmployee.setDateOfBirth(null);

        assertThrows(IllegalArgumentException.class,
                () -> employeeService.saveEmployee(sampleEmployee));
    }

    @Test
    void saveEmployee_throwsWhenDepartmentNull() {
        sampleEmployee.setDepartment(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.saveEmployee(sampleEmployee));

        assertTrue(ex.getMessage().contains("Department is required"));
    }

    @Test
    void saveEmployee_throwsWhenDepartmentDoesNotExist() {
        // validateEmployee() reaches the department existence check before the duplicate-ID check,
        // so only departmentRepository needs to be stubbed here
        when(departmentRepository.existsById(1L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.saveEmployee(sampleEmployee));

        assertTrue(ex.getMessage().contains("does not exist"));
    }

    @Test
    void saveEmployee_throwsWhenSalaryZero() {
        sampleEmployee.setSalary(0.0);

        assertThrows(IllegalArgumentException.class,
                () -> employeeService.saveEmployee(sampleEmployee));
    }

    // ── updateEmployee ────────────────────────────────────────────────────────

    @Test
    void updateEmployee_success() {
        Department hr = new Department("HR", "Human resources");
        hr.setId(2L);
        Employee updated = new Employee("EMP001", "Jane Doe",
                LocalDate.of(1992, 3, 20), hr, 80000.0);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        when(departmentRepository.existsById(2L)).thenReturn(true);
        when(employeeRepository.save(any(Employee.class))).thenReturn(sampleEmployee);

        Employee result = employeeService.updateEmployee(1L, updated);

        assertNotNull(result);
        assertEquals("Jane Doe", sampleEmployee.getName());
        assertEquals(hr, sampleEmployee.getDepartment());
        assertEquals(80000.0, sampleEmployee.getSalary());
    }

    @Test
    void updateEmployee_throwsWhenNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.updateEmployee(99L, sampleEmployee));

        assertTrue(ex.getMessage().contains("Employee not found with id: 99"));
    }

    // ── deleteEmployee ────────────────────────────────────────────────────────

    @Test
    void deleteEmployee_success() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(1L);

        assertDoesNotThrow(() -> employeeService.deleteEmployee(1L));
        verify(employeeRepository).deleteById(1L);
    }

    @Test
    void deleteEmployee_throwsWhenNotFound() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.deleteEmployee(99L));

        assertTrue(ex.getMessage().contains("Employee not found with id: 99"));
    }

    // ── getEmployeeById ───────────────────────────────────────────────────────

    @Test
    void getEmployeeById_success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));

        Employee result = employeeService.getEmployeeById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getEmployeeById_throwsWhenNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> employeeService.getEmployeeById(99L));
    }

    // ── getEmployeeByEmployeeId ───────────────────────────────────────────────

    @Test
    void getEmployeeByEmployeeId_success() {
        when(employeeRepository.findByEmployeeId("EMP001")).thenReturn(Optional.of(sampleEmployee));

        Employee result = employeeService.getEmployeeByEmployeeId("EMP001");

        assertNotNull(result);
        assertEquals("EMP001", result.getEmployeeId());
    }

    @Test
    void getEmployeeByEmployeeId_throwsWhenNotFound() {
        when(employeeRepository.findByEmployeeId("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> employeeService.getEmployeeByEmployeeId("UNKNOWN"));
    }

    // ── getAllEmployees ───────────────────────────────────────────────────────

    @Test
    void getAllEmployees_returnsList() {
        when(employeeRepository.findAll()).thenReturn(List.of(sampleEmployee));

        assertEquals(1, employeeService.getAllEmployees().size());
    }

    @Test
    void getAllEmployees_returnsEmptyList() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        assertTrue(employeeService.getAllEmployees().isEmpty());
    }

    // ── searchEmployeesByName ─────────────────────────────────────────────────

    @Test
    void searchEmployeesByName_success() {
        when(employeeRepository.findByNameContainingIgnoreCase("John"))
                .thenReturn(List.of(sampleEmployee));

        List<Employee> result = employeeService.searchEmployeesByName("John");

        assertEquals(1, result.size());
    }

    @Test
    void searchEmployeesByName_throwsWhenBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.searchEmployeesByName("  "));
    }

    @Test
    void searchEmployeesByName_throwsWhenNull() {
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.searchEmployeesByName(null));
    }

    // ── getEmployeesByDepartment ──────────────────────────────────────────────

    @Test
    void getEmployeesByDepartment_success() {
        when(departmentRepository.findByName("Engineering")).thenReturn(Optional.of(sampleDept));
        when(employeeRepository.findByDepartment(sampleDept)).thenReturn(List.of(sampleEmployee));

        List<Employee> result = employeeService.getEmployeesByDepartment("Engineering");

        assertEquals(1, result.size());
    }

    @Test
    void getEmployeesByDepartment_throwsWhenDepartmentNotFound() {
        when(departmentRepository.findByName("Finance")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> employeeService.getEmployeesByDepartment("Finance"));
    }

    @Test
    void getEmployeesByDepartment_throwsWhenBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.getEmployeesByDepartment(""));
    }

    // ── calculateAverageSalary ────────────────────────────────────────────────

    @Test
    void calculateAverageSalary_returnsCorrectAverage() {
        Employee emp2 = new Employee("EMP002", "Jane",
                LocalDate.of(1985, 1, 1), sampleDept, 85000.0);
        when(employeeRepository.findAll()).thenReturn(Arrays.asList(sampleEmployee, emp2));

        assertEquals(80000.0, employeeService.calculateAverageSalary(), 0.01);
    }

    @Test
    void calculateAverageSalary_returnsZeroWhenEmpty() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        assertEquals(0.0, employeeService.calculateAverageSalary());
    }

    // ── calculateAverageAge ───────────────────────────────────────────────────

    @Test
    void calculateAverageAge_returnsPositiveValue() {
        when(employeeRepository.findAll()).thenReturn(List.of(sampleEmployee));

        assertTrue(employeeService.calculateAverageAge() > 0);
    }

    @Test
    void calculateAverageAge_returnsZeroWhenEmpty() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        assertEquals(0.0, employeeService.calculateAverageAge());
    }

    // ── getEmployeesOrderedByDepartment ───────────────────────────────────────

    @Test
    void getEmployeesOrderedByDepartment_delegatesToRepository() {
        when(employeeRepository.findAllOrderByDepartment()).thenReturn(List.of(sampleEmployee));

        List<Employee> result = employeeService.getEmployeesOrderedByDepartment();

        assertEquals(1, result.size());
        verify(employeeRepository).findAllOrderByDepartment();
    }

    // ── getEmployeesOrderedByAge ──────────────────────────────────────────────

    @Test
    void getEmployeesOrderedByAge_returnsSortedList() {
        Employee older = new Employee("EMP003", "Old Bob",
                LocalDate.of(1960, 1, 1), sampleDept, 90000.0);
        Employee younger = new Employee("EMP004", "Young Sue",
                LocalDate.of(2000, 6, 1), sampleDept, 60000.0);
        when(employeeRepository.findAll()).thenReturn(Arrays.asList(older, younger));

        List<Employee> result = employeeService.getEmployeesOrderedByAge();

        assertTrue(result.get(0).getAge() <= result.get(1).getAge());
    }
}