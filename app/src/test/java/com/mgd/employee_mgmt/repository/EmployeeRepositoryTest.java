package com.mgd.employee_mgmt.repository;

import com.mgd.employee_mgmt.model.Employee;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EntityManager entityManager;

    private Employee emp1;
    private Employee emp2;
    private Employee emp3;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        entityManager.flush();  // force DELETEs to DB before INSERTs

        emp1 = new Employee("EMP001", "Alice Smith", LocalDate.of(1990, 3, 10), "Engineering", 80000.0);
        emp2 = new Employee("EMP002", "Bob Jones",   LocalDate.of(1985, 7, 22), "Engineering", 90000.0);
        emp3 = new Employee("EMP003", "Carol White", LocalDate.of(1995, 11, 5), "HR",          60000.0);

        employeeRepository.saveAll(List.of(emp1, emp2, emp3));
        entityManager.flush();  // force INSERTs to DB immediately
    }

    // ─── findByEmployeeId ─────────────────────────────────────────────────────

    @Test
    void findByEmployeeId_returnsEmployee() {
        Optional<Employee> result = employeeRepository.findByEmployeeId("EMP001");
        assertTrue(result.isPresent());
        assertEquals("Alice Smith", result.get().getName());
    }

    @Test
    void findByEmployeeId_returnsEmptyWhenNotFound() {
        Optional<Employee> result = employeeRepository.findByEmployeeId("UNKNOWN");
        assertFalse(result.isPresent());
    }

    // ─── findByDepartment ─────────────────────────────────────────────────────

    @Test
    void findByDepartment_returnsMatchingEmployees() {
        List<Employee> result = employeeRepository.findByDepartment("Engineering");
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> e.getDepartment().equals("Engineering")));
    }

    @Test
    void findByDepartment_returnsEmptyWhenNoneMatch() {
        List<Employee> result = employeeRepository.findByDepartment("Finance");
        assertTrue(result.isEmpty());
    }

    // ─── existsByEmployeeId ───────────────────────────────────────────────────

    @Test
    void existsByEmployeeId_trueWhenExists() {
        assertTrue(employeeRepository.existsByEmployeeId("EMP002"));
    }

    @Test
    void existsByEmployeeId_falseWhenNotExists() {
        assertFalse(employeeRepository.existsByEmployeeId("EMP999"));
    }

    // ─── calculateAverageSalary ───────────────────────────────────────────────

    @Test
    void calculateAverageSalary_returnsCorrectValue() {
        Double avg = employeeRepository.calculateAverageSalary();
        // (80000 + 90000 + 60000) / 3 = 76666.67
        assertNotNull(avg);
        assertEquals(76666.67, avg, 0.01);
    }

    // ─── findAllOrderByDepartment ─────────────────────────────────────────────

    @Test
    void findAllOrderByDepartment_returnsSortedByDepartmentThenName() {
        List<Employee> result = employeeRepository.findAllOrderByDepartment();
        assertEquals(3, result.size());
        assertEquals("Engineering", result.get(0).getDepartment());
        assertEquals("Engineering", result.get(1).getDepartment());
        assertEquals("HR", result.get(2).getDepartment());
    }

    @Test
    void findAllOrderByDepartment_sortsByNameWithinDepartment() {
        List<Employee> result = employeeRepository.findAllOrderByDepartment();
        assertEquals("Alice Smith", result.get(0).getName());
        assertEquals("Bob Jones",   result.get(1).getName());
    }

    // ─── findByNameContainingIgnoreCase ───────────────────────────────────────

    @Test
    void findByNameContainingIgnoreCase_matchesPartialName() {
        List<Employee> result = employeeRepository.findByNameContainingIgnoreCase("alice");
        assertEquals(1, result.size());
        assertEquals("Alice Smith", result.get(0).getName());
    }

    @Test
    void findByNameContainingIgnoreCase_isCaseInsensitive() {
        List<Employee> result = employeeRepository.findByNameContainingIgnoreCase("CAROL");
        assertEquals(1, result.size());
        assertEquals("Carol White", result.get(0).getName());
    }

    @Test
    void findByNameContainingIgnoreCase_returnsMultipleMatches() {
        List<Employee> result = employeeRepository.findByNameContainingIgnoreCase("smith");
        assertEquals(1, result.size());
    }

    @Test
    void findByNameContainingIgnoreCase_returnsEmptyWhenNoMatch() {
        List<Employee> result = employeeRepository.findByNameContainingIgnoreCase("xyz");
        assertTrue(result.isEmpty());
    }

    // ─── findBySalaryGreaterThanEqual ─────────────────────────────────────────

    @Test
    void findBySalaryGreaterThanEqual_returnsCorrectEmployees() {
        List<Employee> result = employeeRepository.findBySalaryGreaterThanEqual(80000.0);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> e.getSalary() >= 80000.0));
    }

    @Test
    void findBySalaryGreaterThanEqual_returnsAllWhenThresholdLow() {
        List<Employee> result = employeeRepository.findBySalaryGreaterThanEqual(1.0);
        assertEquals(3, result.size());
    }

    @Test
    void findBySalaryGreaterThanEqual_returnsEmptyWhenThresholdTooHigh() {
        List<Employee> result = employeeRepository.findBySalaryGreaterThanEqual(999999.0);
        assertTrue(result.isEmpty());
    }

    // ─── General JPA (save, findById, delete) ─────────────────────────────────

    @Test
    void save_persistsNewEmployee() {
        Employee newEmp = new Employee("EMP004", "Dave Brown", LocalDate.of(1988, 6, 1), "Finance", 70000.0);
        Employee saved = employeeRepository.save(newEmp);
        assertNotNull(saved.getId());
        assertEquals("EMP004", saved.getEmployeeId());
    }

    @Test
    void findById_returnsEmployee() {
        Employee saved = employeeRepository.save(
                new Employee("EMP005", "Eve Black", LocalDate.of(1993, 2, 14), "IT", 65000.0));
        Optional<Employee> result = employeeRepository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("Eve Black", result.get().getName());
    }

    @Test
    void deleteById_removesEmployee() {
        Long id = emp1.getId();
        employeeRepository.deleteById(id);
        assertFalse(employeeRepository.existsById(id));
    }

    @Test
    void findAll_returnsAllEmployees() {
        List<Employee> all = employeeRepository.findAll();
        assertEquals(3, all.size());
    }
}