package com.comp5348.Store.controller;

import com.comp5348.Store.model.User;
import com.comp5348.Store.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        String result = authService.register(user);
        if (result.equals("User registered successfully.")) {
            return ResponseEntity.status(201).body(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User loginRequest, HttpSession session) {
        boolean success = authService.login(loginRequest.getUsername(), loginRequest.getPassword());

        if (success) {
            session.setAttribute("userId", loginRequest.getUsername());
            return ResponseEntity.ok("Login successful.");
        }
        return ResponseEntity.badRequest().body("Invalid username or password.");
    }
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Backend is working!");
    }
}
