package com.olie.api.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.olie.api.entity.RevokedToken;
import com.olie.api.repository.RevokedTokenRepository;
import com.olie.api.security.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMinutes;
    private final RevokedTokenRepository revokedTokenRepository;

    public JwtService(JwtProperties jwtProperties, RevokedTokenRepository revokedTokenRepository) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = jwtProperties.expirationMinutes();
        this.revokedTokenRepository = revokedTokenRepository;
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMinutes * 60 * 1000);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token) && !isTokenRevoked(token);
    }

    public void revoke(String token) {
        Claims claims = parseClaims(token);
        revokedTokenRepository.save(new RevokedToken(claims.getId(), claims.getExpiration().toInstant()));
    }

    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    private boolean isTokenRevoked(String token) {
        return revokedTokenRepository.existsById(parseClaims(token).getId());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
