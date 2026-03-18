package com.mgd.employee_mgmt.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void passwordEncoder_beanLoads() {
        assertNotNull(passwordEncoder);
    }

    @Test
    void passwordEncoder_encodesPassword() {
        String encoded = passwordEncoder.encode("mypassword");
        assertNotNull(encoded);
        assertNotEquals("mypassword", encoded);
    }

    @Test
    void passwordEncoder_matchesCorrectPassword() {
        String encoded = passwordEncoder.encode("secret");
        assertTrue(passwordEncoder.matches("secret", encoded));
    }

    @Test
    void passwordEncoder_doesNotMatchWrongPassword() {
        String encoded = passwordEncoder.encode("secret");
        assertFalse(passwordEncoder.matches("wrong", encoded));
    }
}