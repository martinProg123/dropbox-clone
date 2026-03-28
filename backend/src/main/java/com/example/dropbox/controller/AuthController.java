package com.example.dropbox.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
                                .httpOnly(true)
                                
                                .path("/")
                                .maxAge(3600)
                                .build();
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .body("Register and login successful");
        }

        @PostMapping("/login")
        public ResponseEntity<String> login(@Valid @RequestBody LoginRequest req) {
                String jwtToken = authService.login(req);
                ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken)
                                .httpOnly(true)
                                
                                .path("/")
                                .maxAge(3600)
                                .build();
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .body("Login successful");
        }

        @PostMapping("/logout")
        public ResponseEntity<String> logout() {
                ResponseCookie cookie = ResponseCookie.from("jwt", "")
                                .httpOnly(true)
                                
                                .path("/")
                                .maxAge(0)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .body("Logged out , bye");
        }

        @GetMapping("/me")
        public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal String email) {
                if (email == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
                }
                return ResponseEntity.ok().body(Map.of("email", email));
        }

}
