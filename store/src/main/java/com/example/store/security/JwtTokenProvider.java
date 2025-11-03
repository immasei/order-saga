package com.example.store.security;

import com.example.store.dto.account.UserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenProvider {

    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserDTO userDto) {
        // access token: short-lived token
        // used for authenticating API requests without re-login
        // currently it's 20 minutes
        // (since we don't have a frontend yet to handle automatic token refresh)
        return Jwts.builder()
                .subject(userDto.getId().toString())
                .claim("email", userDto.getEmail())
                .claim("username", userDto.getUsername().toString())
                .claim("role", userDto.getRole().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 20))
                .signWith(getSecretKey())
                .compact();
    }

    public String generateRefreshToken(UserDTO userDto) {
        // refresh token: long-lived token (expires 14 days after issuance)
        // used to obtain new access tokens without requiring user re-authentication
        return Jwts.builder()
                .subject(userDto.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 14))
                .signWith(getSecretKey())
                .compact();
    }

    public UUID getUserIdFromToken(String token) {
        // used for both access and refresh token
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token) // throws ExpiredJwtException
                .getPayload();
        return UUID.fromString(claims.getSubject());
    }

}