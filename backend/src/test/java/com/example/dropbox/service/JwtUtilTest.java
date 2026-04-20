package com.example.dropbox.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "mySecretKey1234567890mySecretKey1234567890");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
        jwtUtil.init();
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        String token = jwtUtil.generateToken("test@example.com");

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void validateToken_ShouldReturnClaimsForValidToken() {
        String token = jwtUtil.generateToken("test@example.com");

        Claims claims = jwtUtil.validateToken(token);

        assertNotNull(claims);
        assertEquals("test@example.com", claims.getSubject());
    }

    @Test
    void validateToken_ShouldReturnNullForInvalidToken() {
        Claims claims = jwtUtil.validateToken("invalid.token.here");

        assertNull(claims);
    }

    @Test
    void validateToken_ShouldReturnNullForExpiredToken() {
        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjAwMDAwMDAwLCJleHAiOjE2MDAwMDAwMDB9.invalid";

        Claims claims = jwtUtil.validateToken(expiredToken);

        assertNull(claims);
    }
}
