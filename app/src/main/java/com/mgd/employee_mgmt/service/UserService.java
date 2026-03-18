package com.mgd.employee_mgmt.service;

import com.mgd.employee_mgmt.exception.InvalidCredentialsException;
import com.mgd.employee_mgmt.model.User;
import com.mgd.employee_mgmt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void register(String username, String password) {
        if (username == null || username.trim().isEmpty())
            throw new IllegalArgumentException("Username is required.");

        if (password == null || password.trim().isEmpty())
            throw new IllegalArgumentException("Password is required.");

        if (userRepository.existsByUsername(username.trim()))
            throw new IllegalArgumentException("Username already taken. Please choose another.");

        User user = new User(username.trim(), passwordEncoder.encode(password), "ROLE_USER");
        userRepository.save(user);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password."));

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new InvalidCredentialsException("Invalid username or password.");

        return user;
    }
}