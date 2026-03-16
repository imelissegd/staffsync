package com.mgd.employee_mgmt.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void defaultConstructor_setsDefaultRole() {
        User user = new User();
        assertEquals("ROLE_USER", user.getRole());
    }

    @Test
    void parameterizedConstructor_setsAllFields() {
        User user = new User();
        user.setUsername("alice");
        user.setPassword("hashed_pw");
        user.setRole("ROLE_ADMIN");

        assertEquals("alice", user.getUsername());
        assertEquals("hashed_pw", user.getPassword());
        assertEquals("ROLE_ADMIN", user.getRole());
    }

    @Test
    void setters_updateFieldsCorrectly() {
        User user = new User();
        user.setId(10L);
        user.setUsername("bob");
        user.setPassword("secret");
        user.setRole("ROLE_USER");

        assertEquals(10L, user.getId());
        assertEquals("bob", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("ROLE_USER", user.getRole());
    }

    @Test
    void idIsNullBeforePersistence() {
        User user = new User();
        user.setUsername("charlie");
        user.setPassword("pw");
        assertNull(user.getId());
    }
}