package com.mgd.employee_mgmt.repository;

import com.mgd.employee_mgmt.model.Department;
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
class DepartmentRepositoryTest {

    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private EmployeeRepository   employeeRepository;
    @Autowired private EntityManager        entityManager;

    private Department engineering;
    private Department hr;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        entityManager.flush();

        engineering = new Department("Engineering", "Software and hardware engineers");
        hr          = new Department("HR", "Human resources");

        departmentRepository.saveAll(List.of(engineering, hr));
        entityManager.flush();
    }

    // ── findByName ────────────────────────────────────────────────────────────

    @Test
    void findByName_returnsMatchingDepartment() {
        Optional<Department> result = departmentRepository.findByName("Engineering");
        assertTrue(result.isPresent());
        assertEquals("Engineering", result.get().getName());
    }

    @Test
    void findByName_returnsEmptyWhenNotFound() {
        Optional<Department> result = departmentRepository.findByName("Finance");
        assertFalse(result.isPresent());
    }

    // ── existsByName ──────────────────────────────────────────────────────────

    @Test
    void existsByName_trueWhenExists() {
        assertTrue(departmentRepository.existsByName("HR"));
    }

    @Test
    void existsByName_falseWhenNotExists() {
        assertFalse(departmentRepository.existsByName("Marketing"));
    }

    // ── existsByDepartment on EmployeeRepository ──────────────────────────────

    @Test
    void existsByDepartment_trueWhenEmployeesAssigned() {
        Employee emp = new Employee("EMP001", "John Doe",
                LocalDate.of(1990, 1, 1), engineering, 75000.0);
        employeeRepository.save(emp);
        entityManager.flush();

        assertTrue(employeeRepository.existsByDepartment(engineering));
    }

    @Test
    void existsByDepartment_falseWhenNoEmployeesAssigned() {
        assertFalse(employeeRepository.existsByDepartment(hr));
    }

    // ── General JPA ───────────────────────────────────────────────────────────

    @Test
    void save_persistsNewDepartment() {
        Department finance = new Department("Finance", "Finance team");
        Department saved   = departmentRepository.save(finance);
        assertNotNull(saved.getId());
        assertEquals("Finance", saved.getName());
    }

    @Test
    void findAll_returnsAllDepartments() {
        List<Department> all = departmentRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void deleteById_removesDepartment() {
        Long id = engineering.getId();
        departmentRepository.deleteById(id);
        assertFalse(departmentRepository.existsById(id));
    }

    @Test
    void save_throwsOnDuplicateName() {
        Department duplicate = new Department("Engineering", "Another engineering dept");
        assertThrows(Exception.class, () -> {
            departmentRepository.save(duplicate);
            entityManager.flush();
        });
    }
}