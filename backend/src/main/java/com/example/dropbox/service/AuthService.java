package com.example.dropbox.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dropbox.dto.auth.LoginRequest;
import com.example.dropbox.dto.auth.RegisterRequest;
import com.example.dropbox.exception.AuthException;
import com.example.dropbox.model.Users;
import com.example.dropbox.repository.UsersRepository;


@Service
public class AuthService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(UsersRepository usersRepository, PasswordEncoder p, JwtUtil j) {
        this.usersRepository = usersRepository;
        passwordEncoder = p;
        jwtUtil = j;
    }

    @Transactional
    public String createUser(RegisterRequest req) {
        if (usersRepository.findByEmail(req.email()) != null) {
            throw new AuthException("Email already in use!");
        }
        if (!req.password().equals(req.confirmPw())) {
            throw new AuthException("Password mismatch");
        }

        Users savedUser = new Users();
        savedUser.setEmail(req.email());
        String hashedPassword = passwordEncoder.encode(req.password());
        savedUser.setPasswordHash(hashedPassword);
        Users u = usersRepository.save(savedUser);
        return jwtUtil.generateToken(u.getEmail());
    }

    @Transactional
    public String login(LoginRequest req) {
        Users u = usersRepository.findByEmail(req.email());
        if (u == null) {
            throw new AuthException("User not Found!");
        }

        if (passwordEncoder.matches(req.password(), u.getPasswordHash()))
            return jwtUtil.generateToken(req.email());
        else
            throw new AuthException("Log In failed! Try again");
    }

}
