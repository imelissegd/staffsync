package com.mgd.employee_mgmt.service;

import java.util.NoSuchElementException;    
import com.mgd.employee_mgmt.exception.InvalidCredentialsException;
import com.mgd.employee_mgmt.model.User;
import com.mgd.employee_mgmt.repository.UserRepository;
import com.mgd.employee_mgmt.util.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository  userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private UserService userService;

    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        ReloadableResourceBundleMessageSource source =
                new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:messages");
        source.setDefaultEncoding("UTF-8");
        MessageUtil messageUtil = new MessageUtil(source);

        userService = new UserService(userRepository, passwordEncoder, messageUtil);

        adminUser   = new User("admin", "hashed_pw", "ROLE_ADMIN");
        adminUser.setId(1L);

        regularUser = new User("alice", "hashed_pw", "ROLE_USER");
        regularUser.setId(2L);
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    void register_success() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed_pw");
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

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

    // ── adminRegister ─────────────────────────────────────────────────────────

    @Test
    void adminRegister_successAsUser() {
        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed_pw");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> userService.adminRegister("bob", "secret", "ROLE_USER"));

        verify(userRepository).save(argThat(u -> "ROLE_USER".equals(u.getRole())));
    }

    @Test
    void adminRegister_successAsAdmin() {
        when(userRepository.existsByUsername("newadmin")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed_pw");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> userService.adminRegister("newadmin", "secret", "ROLE_ADMIN"));

        verify(userRepository).save(argThat(u -> "ROLE_ADMIN".equals(u.getRole())));
    }

    @Test
    void adminRegister_defaultsToUserWhenRoleInvalid() {
        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed_pw");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> userService.adminRegister("bob", "secret", "INVALID_ROLE"));

        verify(userRepository).save(argThat(u -> "ROLE_USER".equals(u.getRole())));
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_success() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(regularUser));
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
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(regularUser));
        when(passwordEncoder.matches("wrong", "hashed_pw")).thenReturn(false);

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> userService.login("alice", "wrong"));
        assertTrue(ex.getMessage().contains("Invalid username or password"));
    }

    @Test
    void login_throwsWhenAccountInactive() {
        regularUser.setActive(false);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(regularUser));

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> userService.login("alice", "secret"));
        assertTrue(ex.getMessage().contains("deactivated"));
    }

    // ── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    void getAllUsers_returnsList() {
        when(userRepository.findAllByOrderByUsernameAsc())
                .thenReturn(List.of(adminUser, regularUser));

        List<User> result = userService.getAllUsers();
        assertEquals(2, result.size());
    }

    // ── promoteUser ───────────────────────────────────────────────────────────

    @Test
    void promoteUser_success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

        User result = userService.promoteUser(2L);
        assertEquals("ROLE_ADMIN", result.getRole());
    }

    @Test
    void promoteUser_throwsWhenAlreadyAdmin() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> userService.promoteUser(1L));
        assertTrue(ex.getMessage().contains("already an admin"));
    }

    @Test
    void promoteUser_throwsWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> userService.promoteUser(99L));
    }

    // ── demoteUser ────────────────────────────────────────────────────────────

    @Test
    void demoteUser_success_whenAnotherAdminExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRoleAndActiveTrue("ROLE_ADMIN")).thenReturn(2L);
        when(userRepository.save(any(User.class))).thenReturn(adminUser);

        User result = userService.demoteUser(1L);
        assertEquals("ROLE_USER", result.getRole());
    }

    @Test
    void demoteUser_throwsWhenLastAdmin() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRoleAndActiveTrue("ROLE_ADMIN")).thenReturn(1L);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> userService.demoteUser(1L));
        assertTrue(ex.getMessage().contains("at least one admin"));
    }

    @Test
    void demoteUser_throwsWhenAlreadyUser() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> userService.demoteUser(2L));
        assertTrue(ex.getMessage().contains("already a regular user"));
    }

    @Test
    void demoteUser_throwsWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> userService.demoteUser(99L));
    }

    // ── activateUser ──────────────────────────────────────────────────────────

    @Test
    void activateUser_success() {
        regularUser.setActive(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

        User result = userService.activateUser(2L);
        assertTrue(result.isActive());
    }

    @Test
    void activateUser_throwsWhenAlreadyActive() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> userService.activateUser(2L));
        assertTrue(ex.getMessage().contains("already active"));
    }

    @Test
    void activateUser_throwsWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> userService.activateUser(99L));
    }

    // ── deactivateUser ────────────────────────────────────────────────────────

    @Test
    void deactivateUser_success_regularUser() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

        User result = userService.deactivateUser(2L);
        assertFalse(result.isActive());
    }

    @Test
    void deactivateUser_success_adminWhenAnotherAdminExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRoleAndActiveTrue("ROLE_ADMIN")).thenReturn(2L);
        when(userRepository.save(any(User.class))).thenReturn(adminUser);

        User result = userService.deactivateUser(1L);
        assertFalse(result.isActive());
    }

    @Test
    void deactivateUser_throwsWhenLastActiveAdmin() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRoleAndActiveTrue("ROLE_ADMIN")).thenReturn(1L);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> userService.deactivateUser(1L));
        assertTrue(ex.getMessage().contains("at least one admin"));
    }

    @Test
    void deactivateUser_throwsWhenAlreadyInactive() {
        regularUser.setActive(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> userService.deactivateUser(2L));
        assertTrue(ex.getMessage().contains("already inactive"));
    }

    @Test
    void deactivateUser_throwsWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> userService.deactivateUser(99L));
    }
}
