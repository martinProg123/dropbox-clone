package com.example.dropbox.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Username is required") @Email(message = "Invalid email format") String email,
        @NotBlank @Size(min = 8) String password) {
    
}
