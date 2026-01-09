package org.example.expensetracker.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    @NotBlank
    @Size(min = 6, max = 30, message = "Password must be between 6 and 30 characters")
    private String password;

    @NotBlank
    private String username;

    @Email
    private String email;
}

