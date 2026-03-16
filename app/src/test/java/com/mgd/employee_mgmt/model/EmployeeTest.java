package com.mgd.employee_mgmt.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee(
                "EMP001",
                "John Doe",
                LocalDate.of(1990, 5, 15),
                "Engineering",
                75000.0
        );
    }

    // ─── Constructor & Getters ────────────────────────────────────────────────

    @Test
    void constructor_setsFieldsCorrectly() {
        assertEquals("EMP001", employee.getEmployeeId());
        assertEquals("John Doe", employee.getName());
        assertEquals(LocalDate.of(1990, 5, 15), employee.getDateOfBirth());
        assertEquals("Engineering", employee.getDepartment());
        assertEquals(75000.0, employee.getSalary());
    }

    @Test
    void defaultConstructor_doesNotThrow() {
        assertDoesNotThrow(() -> new Employee());
    }

    // ─── Inherited getAge() from Person ──────────────────────────────────────

    @Test
    void getAge_returnsCorrectAge() {
        int expectedAge = LocalDate.now().getYear() - 1990;
        int actualAge = employee.getAge();
        // Account for whether birthday has passed this year
        assertTrue(actualAge == expectedAge || actualAge == expectedAge - 1);
    }

    @Test
    void getAge_returnsZeroWhenDateOfBirthNull() {
        employee.setDateOfBirth(null);
        assertEquals(0, employee.getAge());
    }

    // ─── isValidSalary ────────────────────────────────────────────────────────

    @Test
    void isValidSalary_trueForPositiveSalary() {
        assertTrue(employee.isValidSalary());
    }

    @Test
    void isValidSalary_falseWhenSalaryZero() {
        employee.setSalary(0.0);
        assertFalse(employee.isValidSalary());
    }

    @Test
    void isValidSalary_falseWhenSalaryNegative() {
        employee.setSalary(-500.0);
        assertFalse(employee.isValidSalary());
    }

    @Test
    void isValidSalary_falseWhenSalaryNull() {
        employee.setSalary(null);
        assertFalse(employee.isValidSalary());
    }

    // ─── getDetails / toString ────────────────────────────────────────────────

    @Test
    void getDetails_containsKeyFields() {
        String details = employee.getDetails();
        assertTrue(details.contains("EMP001"));
        assertTrue(details.contains("John Doe"));
        assertTrue(details.contains("Engineering"));
        assertTrue(details.contains("75000.00"));
    }

    @Test
    void toString_matchesGetDetails() {
        assertEquals(employee.getDetails(), employee.toString());
    }

    // ─── Setters ──────────────────────────────────────────────────────────────

    @Test
    void setters_updateFieldsCorrectly() {
        employee.setId(42L);
        employee.setEmployeeId("EMP999");
        employee.setName("Jane Smith");
        employee.setDepartment("HR");
        employee.setSalary(90000.0);
        employee.setDateOfBirth(LocalDate.of(1995, 3, 10));

        assertEquals(42L, employee.getId());
        assertEquals("EMP999", employee.getEmployeeId());
        assertEquals("Jane Smith", employee.getName());
        assertEquals("HR", employee.getDepartment());
        assertEquals(90000.0, employee.getSalary());
        assertEquals(LocalDate.of(1995, 3, 10), employee.getDateOfBirth());
    }
}