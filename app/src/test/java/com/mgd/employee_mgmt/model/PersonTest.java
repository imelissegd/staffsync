package com.mgd.employee_mgmt.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class PersonTest {

    static class TestPerson extends Person {
        public TestPerson(String name, LocalDate dob) {
            super(name, dob);
        }
        @Override
        public String getDetails() {
            return "TestPerson[" + getName() + "]";
        }
    }

    @Test
    void getAge_returnsCorrectAge() {
        LocalDate dob = LocalDate.now().minusYears(30);
        TestPerson p = new TestPerson("Test", dob);
        assertEquals(30, p.getAge());
    }

    @Test
    void getAge_returnsZeroWhenDateOfBirthNull() {
        TestPerson p = new TestPerson("Test", null);
        assertEquals(0, p.getAge());
    }

    @Test
    void getAge_accountsForBirthdayNotYetOccurred() {
        LocalDate dob = LocalDate.now().plusDays(1).minusYears(25);
        TestPerson p = new TestPerson("Test", dob);
        assertEquals(24, p.getAge());
    }

    @Test
    void getDetails_abstractMethodEnforced() {
        TestPerson p = new TestPerson("Alice", LocalDate.of(1990, 1, 1));
        assertTrue(p.getDetails().contains("Alice"));
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        TestPerson p = new TestPerson("Alice", LocalDate.of(1990, 5, 20));
        assertEquals("Alice", p.getName());
        assertEquals(LocalDate.of(1990, 5, 20), p.getDateOfBirth());
        p.setName("Bob");
        p.setDateOfBirth(LocalDate.of(1995, 3, 10));
        assertEquals("Bob", p.getName());
        assertEquals(LocalDate.of(1995, 3, 10), p.getDateOfBirth());
    }
}