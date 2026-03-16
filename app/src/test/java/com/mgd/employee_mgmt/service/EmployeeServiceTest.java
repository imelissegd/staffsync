package com.mgd.employee_mgmt.service;

import com.mgd.employee_mgmt.model.Employee;
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

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee sampleEmployee;

    @BeforeEach
    void setUp() {
        sampleEmployee = new Employee(
                "EMP001",
                "John Doe",
                LocalDate.of(1990, 5, 15),
                "Engineering",
                75000.0
        );
        sampleEmployee.setId(1L);
    }

    // ─── saveEmployee ─────────────────────────────────────────────────────────

    @Test
    void saveEmployee_success() {
        when(employeeRepository.existsByEmployeeId("EMP001")).thenReturn(false);
        when(employeeRepository.save(sampleEmployee)).thenReturn(sampleEmployee);

        Employee result = employeeService.saveEmployee(sampleEmployee);

        assertNotNull(result);
        assertEquals("EMP001", result.getEmployeeId());
        verify(employeeRepository).save(sampleEmployee);
    }

    @Test
    void saveEmployee_throwsWhenDuplicateEmployeeId() {
        when(employeeRepository.existsByEmployeeId("EMP001")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.saveEmployee(sampleEmployee));

        assertTrue(ex.getMessage().contains("already exists"));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void saveEmployee_throwsWhenEmployeeIdBlank() {
        sampleEmployee.setEmployeeId("  ");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.saveEmployee(sampleEmployee));

        assertTrue(ex.getMessage().contains("Employee ID is required"));
    }

    @Test
    void saveEmployee_throwsWhenNameBlank() {
        sampleEmployee.setName("");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.saveEmployee(sampleEmployee));

        assertTrue(ex.getMessage().contains("Employee name is required"));
    }

    @Test
    void saveEmployee_throwsWhenDateOfBirthNull() {
        sampleEmployee.setDateOfBirth(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.saveEmployee(sampleEmployee));

        assertTrue(ex.getMessage().contains("Date of birth is required"));
    }

    @Test
    void saveEmployee_throwsWhenDepartmentBlank() {
        sampleEmployee.setDepartment("");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.saveEmployee(sampleEmployee));

        assertTrue(ex.getMessage().contains("Department is required"));
    }

    @Test
    void saveEmployee_throwsWhenSalaryZeroOrNegative() {
        sampleEmployee.setSalary(0.0);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.saveEmployee(sampleEmployee));

        assertTrue(ex.getMessage().contains("Salary must be greater than 0"));
    }

    // ─── updateEmployee ───────────────────────────────────────────────────────

    @Test
    void updateEmployee_success() {
        Employee updated = new Employee("EMP001", "Jane Doe", LocalDate.of(1992, 3, 20),
                "HR", 80000.0);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(sampleEmployee);

        Employee result = employeeService.updateEmployee(1L, updated);

        assertNotNull(result);
        verify(employeeRepository).save(sampleEmployee);
        assertEquals("Jane Doe", sampleEmployee.getName());
        assertEquals("HR", sampleEmployee.getDepartment());
        assertEquals(80000.0, sampleEmployee.getSalary());
    }

    @Test
    void updateEmployee_throwsWhenNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.updateEmployee(99L, sampleEmployee));

        assertTrue(ex.getMessage().contains("Employee not found with id: 99"));
    }

    // ─── deleteEmployee ───────────────────────────────────────────────────────

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

    // ─── getEmployeeById ──────────────────────────────────────────────────────

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

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.getEmployeeById(99L));

        assertTrue(ex.getMessage().contains("Employee not found with id: 99"));
    }

    // ─── getEmployeeByEmployeeId ──────────────────────────────────────────────

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

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.getEmployeeByEmployeeId("UNKNOWN"));

        assertTrue(ex.getMessage().contains("Employee not found with employee ID: UNKNOWN"));
    }

    // ─── getAllEmployees ──────────────────────────────────────────────────────

    @Test
    void getAllEmployees_returnsList() {
        when(employeeRepository.findAll()).thenReturn(List.of(sampleEmployee));

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(1, result.size());
    }

    @Test
    void getAllEmployees_returnsEmptyList() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        List<Employee> result = employeeService.getAllEmployees();

        assertTrue(result.isEmpty());
    }

    // ─── searchEmployeesByName ────────────────────────────────────────────────

    @Test
    void searchEmployeesByName_success() {
        when(employeeRepository.findByNameContainingIgnoreCase("John"))
                .thenReturn(List.of(sampleEmployee));

        List<Employee> result = employeeService.searchEmployeesByName("John");

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
    }

    @Test
    void searchEmployeesByName_throwsWhenNameBlank() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.searchEmployeesByName("  "));

        assertTrue(ex.getMessage().contains("Search name cannot be empty"));
    }

    @Test
    void searchEmployeesByName_throwsWhenNameNull() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.searchEmployeesByName(null));

        assertTrue(ex.getMessage().contains("Search name cannot be empty"));
    }

    // ─── getEmployeesByDepartment ─────────────────────────────────────────────

    @Test
    void getEmployeesByDepartment_success() {
        when(employeeRepository.findByDepartment("Engineering"))
                .thenReturn(List.of(sampleEmployee));

        List<Employee> result = employeeService.getEmployeesByDepartment("Engineering");

        assertEquals(1, result.size());
    }

    @Test
    void getEmployeesByDepartment_throwsWhenBlank() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.getEmployeesByDepartment(""));

        assertTrue(ex.getMessage().contains("Department cannot be empty"));
    }

    // ─── calculateAverageSalary ───────────────────────────────────────────────

    @Test
    void calculateAverageSalary_returnsCorrectAverage() {
        Employee emp2 = new Employee("EMP002", "Jane", LocalDate.of(1985, 1, 1),
                "HR", 85000.0);
        when(employeeRepository.findAll()).thenReturn(Arrays.asList(sampleEmployee, emp2));

        double avg = employeeService.calculateAverageSalary();

        assertEquals(80000.0, avg, 0.01);
    }

    @Test
    void calculateAverageSalary_returnsZeroWhenNoEmployees() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        double avg = employeeService.calculateAverageSalary();

        assertEquals(0.0, avg);
    }

    // ─── calculateAverageAge ──────────────────────────────────────────────────

    @Test
    void calculateAverageAge_returnsPositiveValue() {
        when(employeeRepository.findAll()).thenReturn(List.of(sampleEmployee));

        double avg = employeeService.calculateAverageAge();

        assertTrue(avg > 0);
    }

    @Test
    void calculateAverageAge_returnsZeroWhenNoEmployees() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        double avg = employeeService.calculateAverageAge();

        assertEquals(0.0, avg);
    }

    // ─── getEmployeesOrderedByDepartment ──────────────────────────────────────

    @Test
    void getEmployeesOrderedByDepartment_delegatesToRepository() {
        when(employeeRepository.findAllOrderByDepartment()).thenReturn(List.of(sampleEmployee));

        List<Employee> result = employeeService.getEmployeesOrderedByDepartment();

        assertEquals(1, result.size());
        verify(employeeRepository).findAllOrderByDepartment();
    }

    // ─── getEmployeesOrderedByAge ─────────────────────────────────────────────

    @Test
    void getEmployeesOrderedByAge_returnsSortedList() {
        Employee older = new Employee("EMP003", "Old Bob", LocalDate.of(1960, 1, 1),
                "Finance", 90000.0);
        Employee younger = new Employee("EMP004", "Young Sue", LocalDate.of(2000, 6, 1),
                "IT", 60000.0);
        when(employeeRepository.findAll()).thenReturn(Arrays.asList(older, younger));

        List<Employee> result = employeeService.getEmployeesOrderedByAge();

        assertEquals(2, result.size());
        assertTrue(result.get(0).getAge() <= result.get(1).getAge());
    }
}