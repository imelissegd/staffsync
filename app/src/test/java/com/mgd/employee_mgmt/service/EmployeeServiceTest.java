package com.mgd.employee_mgmt.service;

import java.util.NoSuchElementException;
import com.mgd.employee_mgmt.model.Department;
import com.mgd.employee_mgmt.model.Employee;
import com.mgd.employee_mgmt.repository.DepartmentRepository;
import com.mgd.employee_mgmt.repository.EmployeeRepository;
import com.mgd.employee_mgmt.util.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

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

    private EmployeeService employeeService;

    private Department sampleDept;
    private Employee   sampleEmployee;

    @BeforeEach
    void setUp() {
        ReloadableResourceBundleMessageSource source =
                new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:messages");
        source.setDefaultEncoding("UTF-8");
        MessageUtil messageUtil = new MessageUtil(source);

        employeeService = new EmployeeService(
                employeeRepository, departmentRepository, messageUtil);

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

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.saveEmployee(sampleEmployee));
        assertTrue(ex.getMessage().contains("Employee ID is required"));
    }

    @Test
    void saveEmployee_throwsWhenNameBlank() {
        sampleEmployee.setName("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.saveEmployee(sampleEmployee));
        assertTrue(ex.getMessage().contains("Employee name is required"));
    }

    @Test
    void saveEmployee_throwsWhenDateOfBirthNull() {
        sampleEmployee.setDateOfBirth(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.saveEmployee(sampleEmployee));
        assertTrue(ex.getMessage().contains("Date of birth is required"));
    }

    @Test
    void saveEmployee_throwsWhenAgeOutOfRange() {
        sampleEmployee.setDateOfBirth(LocalDate.now().minusYears(10)); // age 10 — too young

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.saveEmployee(sampleEmployee));
        assertTrue(ex.getMessage().contains("age must be between 18 and 100"));
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
        when(departmentRepository.existsById(1L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.saveEmployee(sampleEmployee));
        assertTrue(ex.getMessage().contains("does not exist"));
    }

    @Test
    void saveEmployee_throwsWhenSalaryZero() {
        sampleEmployee.setSalary(0.0);
        when(departmentRepository.existsById(1L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.saveEmployee(sampleEmployee));
        assertTrue(ex.getMessage().contains("Salary must be greater than 0"));
    }

    @Test
    void saveEmployee_throwsWhenSalaryNegative() {
        sampleEmployee.setSalary(-100.0);
        when(departmentRepository.existsById(1L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.saveEmployee(sampleEmployee));
        assertTrue(ex.getMessage().contains("Salary must be greater than 0"));
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

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> employeeService.updateEmployee(99L, sampleEmployee));
        assertTrue(ex.getMessage().contains("Employee not found with id"));
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void updateEmployee_throwsWhenDuplicateEmployeeId() {
        Employee updated = new Employee("EMP002", "Jane Doe",
                LocalDate.of(1992, 3, 20), sampleDept, 80000.0);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(employeeRepository.existsByEmployeeId("EMP002")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.updateEmployee(1L, updated));
        assertTrue(ex.getMessage().contains("already exists"));
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

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> employeeService.deleteEmployee(99L));
        assertTrue(ex.getMessage().contains("Employee not found with id"));
        assertTrue(ex.getMessage().contains("99"));
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

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> employeeService.getEmployeeById(99L));
        assertTrue(ex.getMessage().contains("Employee not found with id"));
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

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> employeeService.getEmployeeByEmployeeId("UNKNOWN"));
        assertTrue(ex.getMessage().contains("Employee not found with employee ID"));
        assertTrue(ex.getMessage().contains("UNKNOWN"));
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
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.searchEmployeesByName("  "));
        assertTrue(ex.getMessage().contains("Search name cannot be empty"));
    }

    @Test
    void searchEmployeesByName_throwsWhenNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.searchEmployeesByName(null));
        assertTrue(ex.getMessage().contains("Search name cannot be empty"));
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

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> employeeService.getEmployeesByDepartment("Finance"));
        assertTrue(ex.getMessage().contains("Department not found with name"));
    }

    @Test
    void getEmployeesByDepartment_throwsWhenBlank() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.getEmployeesByDepartment(""));
        assertTrue(ex.getMessage().contains("Department name cannot be empty"));
    }

    // ── calculateAverageSalary ────────────────────────────────────────────────

    @Test
    void calculateAverageSalary_returnsCorrectAverage() {
        when(employeeRepository.calculateAverageSalary()).thenReturn(80000.0);

        assertEquals(80000.0, employeeService.calculateAverageSalary(), 0.01);
    }

    @Test
    void calculateAverageSalary_returnsZeroWhenEmpty() {
        when(employeeRepository.calculateAverageSalary()).thenReturn(null);

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
        Employee older   = new Employee("EMP003", "Old Bob",
                LocalDate.of(1960, 1, 1), sampleDept, 90000.0);
        Employee younger = new Employee("EMP004", "Young Sue",
                LocalDate.of(2000, 6, 1), sampleDept, 60000.0);
        when(employeeRepository.findAll()).thenReturn(Arrays.asList(older, younger));

        List<Employee> result = employeeService.getEmployeesOrderedByAge();

        assertTrue(result.get(0).getAge() <= result.get(1).getAge());
    }
}
