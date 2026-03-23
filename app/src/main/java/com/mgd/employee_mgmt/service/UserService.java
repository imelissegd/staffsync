package com.mgd.employee_mgmt.service;

import com.mgd.employee_mgmt.exception.InvalidCredentialsException;
import com.mgd.employee_mgmt.model.User;
import com.mgd.employee_mgmt.repository.UserRepository;
import com.mgd.employee_mgmt.util.MessageUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageUtil msg;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       MessageUtil msg) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.msg             = msg;
    }

    public void register(String username, String password) {
        if (username == null || username.trim().isEmpty())
            throw new IllegalArgumentException(msg.get("auth.username.required"));

        if (password == null || password.trim().isEmpty())
            throw new IllegalArgumentException(msg.get("auth.password.required"));

        if (password.length() < 6)
            throw new IllegalArgumentException(msg.get("auth.password.too.short"));

        if (userRepository.existsByUsername(username.trim()))
            throw new IllegalArgumentException(msg.get("auth.username.taken"));

        User user = new User(username.trim(), passwordEncoder.encode(password), "ROLE_USER");
        userRepository.save(user);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException(
                        msg.get("auth.credentials.invalid")));

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new InvalidCredentialsException(msg.get("auth.credentials.invalid"));

        return user;
    }
}
