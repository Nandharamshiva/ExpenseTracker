package org.example.expensetracker.auth.security;

import org.example.expensetracker.auth.entity.User;
import org.example.expensetracker.auth.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            return userRepository.findByUsernameOrEmail(username, username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
        }

        throw new RuntimeException("Invalid authentication principal type");
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }
}

