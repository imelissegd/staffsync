package com.mgd.employee_mgmt.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DepartmentTest {

    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department("Engineering", "Software and hardware engineers");
    }

    // ─── Constructors ─────────────────────────────────────────────────────────

    @Test
    void parameterizedConstructor_setsNameAndDescription() {
        assertEquals("Engineering", department.getName());
        assertEquals("Software and hardware engineers", department.getDescription());
    }

    @Test
    void parameterizedConstructor_withNullDescription() {
        Department d = new Department("HR", null);
        assertEquals("HR", d.getName());
        assertNull(d.getDescription());
    }

    @Test
    void defaultConstructor_doesNotThrow() {
        assertDoesNotThrow(() -> new Department());
    }

    @Test
    void defaultConstructor_fieldsAreNullByDefault() {
        Department d = new Department();
        assertNull(d.getName());
        assertNull(d.getDescription());
    }

    // ─── ID (null before persistence) ────────────────────────────────────────

    @Test
    void id_isNullBeforePersistence() {
        Department d = new Department("Finance", "Finance team");
        assertNull(d.getId());
    }

    @Test
    void setId_updatesId() {
        department.setId(10L);
        assertEquals(10L, department.getId());
    }

    // ─── Name getter & setter ─────────────────────────────────────────────────

    @Test
    void getName_returnsCorrectValue() {
        assertEquals("Engineering", department.getName());
    }

    @Test
    void setName_updatesName() {
        department.setName("IT");
        assertEquals("IT", department.getName());
    }

    @Test
    void setName_allowsNull() {
        department.setName(null);
        assertNull(department.getName());
    }

    // ─── Description getter & setter ─────────────────────────────────────────

    @Test
    void getDescription_returnsCorrectValue() {
        assertEquals("Software and hardware engineers", department.getDescription());
    }

    @Test
    void setDescription_updatesDescription() {
        department.setDescription("Updated description");
        assertEquals("Updated description", department.getDescription());
    }

    @Test
    void setDescription_allowsNull() {
        department.setDescription(null);
        assertNull(department.getDescription());
    }

    // ─── Setters chain (full update) ──────────────────────────────────────────

    @Test
    void allSetters_updateFieldsCorrectly() {
        department.setId(5L);
        department.setName("Marketing");
        department.setDescription("Marketing team");

        assertEquals(5L, department.getId());
        assertEquals("Marketing", department.getName());
        assertEquals("Marketing team", department.getDescription());
    }
}