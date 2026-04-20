package com.example.dropbox.service;

import com.example.dropbox.dto.auth.LoginRequest;
import com.example.dropbox.dto.auth.RegisterRequest;
import com.example.dropbox.exception.AuthException;
import com.example.dropbox.model.Users;
import com.example.dropbox.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usersRepository, passwordEncoder, jwtUtil);
    }

    @Test
    void createUserShouldCreateUserWhenValidRequest() {
        RegisterRequest req = new RegisterRequest("test@example.com", "password123", "password123");
        Users savedUser = new Users();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");

        when(usersRepository.findByEmail("test@example.com")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(usersRepository.save(any(Users.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwtToken");

        String result = authService.createUser(req);

        assertNotNull(result);
        assertEquals("jwtToken", result);
        verify(usersRepository).save(any(Users.class));
    }

    @Test
    void createUser_Should_ThrowException_When_EmailAlreadyExists() {
        RegisterRequest req = new RegisterRequest("existing@example.com", "password123", "password123");
        Users existingUser = new Users();

        when(usersRepository.findByEmail("existing@example.com")).thenReturn(existingUser);

        AuthException exception = assertThrows(AuthException.class, () -> authService.createUser(req));
        assertEquals("Email already in use!", exception.getMessage());
    }

    @Test
    void createUser_Should_ThrowException_When_PasswordMismatch() {
        RegisterRequest req = new RegisterRequest("test@example.com", "password123", "differentPassword");

        AuthException exception = assertThrows(AuthException.class, () -> authService.createUser(req));
        assertEquals("Password mismatch", exception.getMessage());
    }

    @Test
    void login_Should_ReturnToken_When_ValidCredentials() {
        LoginRequest req = new LoginRequest("test@example.com", "password123");
        Users user = new Users();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");

        when(usersRepository.findByEmail("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwtToken");

        String result = authService.login(req);

        assertNotNull(result);
        assertEquals("jwtToken", result);
    }

    @Test
    void login_Should_ThrowException_When_UserNotFound() {
        LoginRequest req = new LoginRequest("notfound@example.com", "password123");

        when(usersRepository.findByEmail("notfound@example.com")).thenReturn(null);

        AuthException exception = assertThrows(AuthException.class, () -> authService.login(req));
        assertEquals("User not Found!", exception.getMessage());
    }

    @Test
    void login_Should_ThrowException_When_WrongPassword() {
        LoginRequest req = new LoginRequest("test@example.com", "wrongPassword");
        Users user = new Users();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");

        when(usersRepository.findByEmail("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        AuthException exception = assertThrows(AuthException.class, () -> authService.login(req));
        assertEquals("Log In failed! Try again", exception.getMessage());
    }
}