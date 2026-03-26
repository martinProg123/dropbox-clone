package com.example.dropbox.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dropbox.dto.auth.LoginRequest;
import com.example.dropbox.dto.auth.RegisterRequest;
import com.example.dropbox.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService a) {
        authService = a;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest req) {
        String jwtToken = authService.createUser(req);
        ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken)
                .httpOnly(true) // Mitigates XSS
                // .secure(true) // Requires HTTPS
                .path("/")
                .maxAge(3600) // 1 hour
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Register and login successful");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest req) {
        String jwtToken = authService.login(req);
        ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken)
                .httpOnly(true) // Mitigates XSS
                // .secure(true) // Requires HTTPS
                .path("/")
                .maxAge(3600) // 1 hour
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Login successful");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // Set age to 0 to delete immediately
                .build();

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body("Logged out , bye");
    }

}
