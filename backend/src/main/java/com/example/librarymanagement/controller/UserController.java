package com.example.librarymanagement.controller;

import com.example.librarymanagement.entity.User;
import com.example.librarymanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            logger.info("Register request received: username=" + user.getUsername());
            User newUser = userService.register(user);
            logger.info("User registered successfully: " + newUser.getUsername());
            return ResponseEntity.ok(newUser);
        } catch (Exception e) {
            logger.error("Registration failed: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for username: " + loginRequest.getUsername());
        User user = userService.findByUsername(loginRequest.getUsername());
        if (user == null) {
            logger.error("User not found: " + loginRequest.getUsername());
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
        logger.info("User found: " + user.getUsername() + ", checking password...");
        logger.info("Stored password hash: " + user.getPassword());
        logger.info("Plain password length: " + loginRequest.getPassword().length());
        
        boolean passwordMatch = userService.checkPassword(loginRequest.getPassword(), user.getPassword());
        logger.info("Password match result: " + passwordMatch);
        
        if (passwordMatch) {
            logger.info("Login successful for user: " + user.getUsername());
            return ResponseEntity.ok("Login successful");
        }
        logger.error("Password mismatch for user: " + loginRequest.getUsername());
        return ResponseEntity.badRequest().body("Invalid credentials");
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}