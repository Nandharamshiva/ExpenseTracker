package org.example.expensetracker.auth.controller;

import org.example.expensetracker.auth.dto.AuthResponse;
import org.example.expensetracker.auth.dto.LoginRequest;
import org.example.expensetracker.auth.dto.SignupRequest;
import org.example.expensetracker.auth.dto.UserResponse;
import org.example.expensetracker.auth.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<String> authInfo() {
        return ResponseEntity.ok("Auth API is running. Use POST /api/auth/signup and POST /api/auth/login.");
    }

    @GetMapping({"/login", "/signup"})
    public ResponseEntity<String> authEndpointsInfo() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body("Use POST for this endpoint.");
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest signupRequest) {
        userService.register(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = userService.login(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(userService.me());
    }
}
