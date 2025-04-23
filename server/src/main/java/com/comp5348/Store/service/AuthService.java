package com.comp5348.Store.service;

import com.comp5348.Store.model.User;
import com.comp5348.Store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return "Username already exists.";
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return "Email already exists.";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User registered successfully.";
    }

    public boolean login(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        return userOptional.isPresent() &&
                passwordEncoder.matches(password, userOptional.get().getPassword());
    }
}