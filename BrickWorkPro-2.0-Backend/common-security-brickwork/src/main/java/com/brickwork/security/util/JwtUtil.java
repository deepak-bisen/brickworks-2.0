package com.brickwork.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import java.util.Arrays;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret:change-me}")
    private String secret;

    @Value("${jwt.expiration:36000000}") // Default to 10 hours if not set in properties
    private Long expiration;

    // METHOD 1: Basic Generation (Standard Spring UserDetails)
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        claims.put("role", role);
        return createToken(claims, userDetails.getUsername());
    }

    // METHOD 2: Advanced Custom Generation (For users-brickwork)
    // We pass the raw values instead of the 'User' entity so the common module
    // doesn't need to know about the users-brickwork database entities!
    public String generateCustomToken(String username, String roleName, String userId) {
        Map<String, Object> claims = new HashMap<>();

        // Ensure "ROLE_" prefix is applied exactly as your original code did
        if (!roleName.startsWith("ROLE_")) {
            claims.put("role", "ROLE_" + roleName);
        } else {
            claims.put("role", roleName);
        }

        claims.put("userId", userId);

        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())
                .compact();
    }

    // --- DECODING & VALIDATION METHODS (Used by all services) ---

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return (String) extractAllClaims(token).get("role");
    }

    // New helper method to easily extract the userId if downstream services need it later
    public String extractUserId(String token) {
        return (String) extractAllClaims(token).get("userId");
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Key getSignKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException e) {
            // Support raw secret strings for backward compatibility when the value
            // is not base64-encoded.
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}