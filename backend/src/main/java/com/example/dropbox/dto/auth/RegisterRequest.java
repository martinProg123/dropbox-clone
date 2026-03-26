package com.example.dropbox.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username is required") @Email(message = "Invalid email format") String email,
        @NotBlank(message = "Password is required")  @Size(min = 8) String password,
        @NotBlank @Size(min = 8) String confirmPw
    ) {
}
