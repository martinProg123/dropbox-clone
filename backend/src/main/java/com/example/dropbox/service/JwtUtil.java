package com.example.dropbox.service;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey SIGNING_KEY;

    @PostConstruct
    public void init() {
        SIGNING_KEY = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email) {
        String jwt = Jwts.builder()
                .subject(email) // Set the subject claim
                .issuedAt(new Date()) // Set the issued at time
                .issuer("com.example.dropbox")
                .expiration(new Date(expiration)) // Set expiration date
                .signWith(SIGNING_KEY) // Sign the token with the key
                .compact(); // Compact the token into its final string format
        return jwt;
    }

    public Claims validateToken(String token) {
        Claims claims = Jwts.parser() // Use the builder for a fluent configuration
                .verifyWith(SIGNING_KEY) // Provide the key used for signing
                .build() // Build the immutable parser instance
                .parseSignedClaims(token) // Parse the JWS (Signed JWT)
                .getPayload(); // Extract the claims (payload)

        // System.out.println("JWT is valid.");
        // System.out.println("Subject: " + claims.getSubject());
        // System.out.println("Issuer: " + claims.getIssuer());
        return claims;
    }
}
