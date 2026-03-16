package com.mgd.employee_mgmt.repository;

import com.mgd.employee_mgmt.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        sampleUser = new User();
        sampleUser.setUsername("alice");
        sampleUser.setPassword("hashed_password");
        sampleUser.setRole("ROLE_USER");

        userRepository.save(sampleUser);
    }

    // ─── findByUsername ───────────────────────────────────────────────────────

    @Test
    void findByUsername_returnsUserWhenExists() {
        Optional<User> result = userRepository.findByUsername("alice");

        assertTrue(result.isPresent());
        assertEquals("alice", result.get().getUsername());
    }

    @Test
    void findByUsername_returnsEmptyWhenNotFound() {
        Optional<User> result = userRepository.findByUsername("unknown");

        assertFalse(result.isPresent());
    }

    // ─── existsByUsername ─────────────────────────────────────────────────────

    @Test
    void existsByUsername_trueWhenExists() {
        assertTrue(userRepository.existsByUsername("alice"));
    }

    @Test
    void existsByUsername_falseWhenNotExists() {
        assertFalse(userRepository.existsByUsername("bob"));
    }

    // ─── save ─────────────────────────────────────────────────────────────────

    @Test
    void save_persistsUser() {
        User newUser = new User();
        newUser.setUsername("bob");
        newUser.setPassword("bob_hashed");
        newUser.setRole("ROLE_USER");

        User saved = userRepository.save(newUser);

        assertNotNull(saved.getId());
        assertEquals("bob", saved.getUsername());
    }

    @Test
    void save_defaultRoleIsRoleUser() {
        User newUser = new User();
        newUser.setUsername("charlie");
        newUser.setPassword("pw");

        User saved = userRepository.save(newUser);

        assertEquals("ROLE_USER", saved.getRole());
    }

    @Test
    void save_throwsOnDuplicateUsername() {
        User duplicate = new User();
        duplicate.setUsername("alice"); // already exists
        duplicate.setPassword("other_pw");

        assertThrows(Exception.class, () -> {
            userRepository.save(duplicate);
            userRepository.flush(); // force DB constraint check
        });
    }

    // ─── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_returnsUserWhenExists() {
        Optional<User> result = userRepository.findById(sampleUser.getId());

        assertTrue(result.isPresent());
        assertEquals("alice", result.get().getUsername());
    }

    @Test
    void findById_returnsEmptyWhenNotFound() {
        Optional<User> result = userRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Test
    void deleteById_removesUser() {
        Long id = sampleUser.getId();
        userRepository.deleteById(id);

        assertFalse(userRepository.existsById(id));
    }

    // ─── findAll ──────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsAllUsers() {
        User second = new User();
        second.setUsername("dave");
        second.setPassword("pw");
        userRepository.save(second);

        assertEquals(2, userRepository.findAll().size());
    }
}