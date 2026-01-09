package org.example.expensetracker.auth.service;

import org.example.expensetracker.auth.dto.AuthResponse;
import org.example.expensetracker.auth.dto.LoginRequest;
import org.example.expensetracker.auth.dto.SignupRequest;
import org.example.expensetracker.auth.dto.UserResponse;
import org.example.expensetracker.auth.entity.Role;
import org.example.expensetracker.auth.entity.User;
import org.example.expensetracker.auth.repository.UserRepository;
import org.example.expensetracker.auth.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public void register(SignupRequest signupRequest) {

        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new IllegalStateException("Username already taken");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalStateException("Email already registered");
        }

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());

        String encodedPassword =
                passwordEncoder.encode(signupRequest.getPassword());
        user.setPassword(encodedPassword);

        user.setRole(Role.USER);
        user.setEnabled(true);

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest loginRequest) {

        User user = userRepository
                .findByUsernameOrEmail(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getUsernameOrEmail()
                )
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid username/email or password"));

        if (!user.isEnabled()) {
            throw new DisabledException("Account is disabled");
        }

        if (!passwordEncoder.matches(
                loginRequest.getPassword(),
                user.getPassword())) {

            throw new BadCredentialsException("Invalid username/email or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        // Prepare user response
        UserResponse userResponse = toUserResponse(user);

        // Return authentication response with token
        return new AuthResponse(token, userResponse);
    }

    public UserResponse me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new BadCredentialsException("Unauthorized");
        }

        String username = auth.getName();
        User user = userRepository
                .findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new BadCredentialsException("Unauthorized"));

        return toUserResponse(user);
    }

    private static UserResponse toUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());
        userResponse.setRole(user.getRole().name());
        return userResponse;
    }

}

