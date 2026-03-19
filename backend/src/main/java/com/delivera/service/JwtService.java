package com.delivera.service;

import com.delivera.model.WorkerRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationSeconds;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration}") long expirationSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(expirationSeconds)))
                .signWith(key)
                .compact();
    }

    public String generateToken(String email, UUID companyId, WorkerRole role) {
        return Jwts.builder()
                .subject(email)
                .claim("companyId", companyId.toString())
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(expirationSeconds)))
                .signWith(key)
                .compact();
    }

    public String parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getClaim(String token, String claimName) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get(claimName, String.class);
    }
}
