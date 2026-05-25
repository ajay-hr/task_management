package com.taskmanagement.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    public String generateAccessToken(String email) {
        return generateToken(email, accessExpirationMs, "access");
    }

    public String generateRefreshToken(String email) {
        return generateToken(email, refreshExpirationMs, "refresh");
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(parseClaims(token).get("type", String.class));
    }

    public boolean isTokenValid(String token, String email) {
        return extractEmail(token).equals(email) && parseClaims(token).getExpiration().after(new Date());
    }

    private String generateToken(String email, long expirationMs, String type) {
        Date now = new Date();

        Key key = signingKey();

        return Jwts.builder()
                .setSubject(email)
                .claim("type", type)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    private Claims parseClaims(String token) {
        Key key = signingKey();

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key signingKey() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }
}
