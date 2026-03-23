package com.mgd.employee_mgmt.service;

import com.mgd.employee_mgmt.exception.InvalidCredentialsException;
import com.mgd.employee_mgmt.model.User;
import com.mgd.employee_mgmt.repository.UserRepository;
import com.mgd.employee_mgmt.util.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository  userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        ReloadableResourceBundleMessageSource source =
                new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:messages");
        source.setDefaultEncoding("UTF-8");
        MessageUtil messageUtil = new MessageUtil(source);

        userService = new UserService(userRepository, passwordEncoder, messageUtil);
        sampleUser  = new User("alice", "hashed_pw", "ROLE_USER");
    }

    // ─── register ──────────────────────────────────────────────────────────

    @Test
    void register_success() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed_pw");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        assertDoesNotThrow(() -> userService.register("alice", "secret"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throwsWhenUsernameBlank() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.register("  ", "secret"));
        assertTrue(ex.getMessage().contains("Username is required"));
    }

    @Test
    void register_throwsWhenUsernameNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.register(null, "secret"));
        assertTrue(ex.getMessage().contains("Username is required"));
    }

    @Test
    void register_throwsWhenPasswordBlank() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.register("alice", ""));
        assertTrue(ex.getMessage().contains("Password is required"));
    }

    @Test
    void register_throwsWhenPasswordTooShort() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.register("alice", "abc"));
        assertTrue(ex.getMessage().contains("at least 6 characters"));
    }

    @Test
    void register_throwsWhenUsernameTaken() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.register("alice", "secret"));
        assertTrue(ex.getMessage().contains("Username already taken"));
        verify(userRepository, never()).save(any());
    }

    // ─── login ─────────────────────────────────────────────────────────────

    @Test
    void login_success() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("secret", "hashed_pw")).thenReturn(true);

        User result = userService.login("alice", "secret");
        assertNotNull(result);
        assertEquals("alice", result.getUsername());
    }

    @Test
    void login_throwsWhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> userService.login("unknown", "pass"));
        assertTrue(ex.getMessage().contains("Invalid username or password"));
    }

    @Test
    void login_throwsWhenPasswordWrong() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrong", "hashed_pw")).thenReturn(false);

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> userService.login("alice", "wrong"));
        assertTrue(ex.getMessage().contains("Invalid username or password"));
    }
}
