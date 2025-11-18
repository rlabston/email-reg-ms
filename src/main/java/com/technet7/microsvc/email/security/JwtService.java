package com.technet7.microsvc.email.security;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;
    private final long slidingExpirationMs;

    public JwtService(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.expiration-ms:3600000}") long expirationMs,
        @Value("${app.jwt.sliding-expiration-ms:900000}") long slidingExpirationMs) {
        // If the provided secret is not base64, use raw bytes for HMAC key
        byte[] keyBytes = secret.matches("^[A-Za-z0-9+/=]+$") && secret.length() % 4 == 0
                ? Decoders.BASE64.decode(secret)
                : secret.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
        this.slidingExpirationMs = slidingExpirationMs;
    }

    public String generateToken(String subject, String username, Set<String> roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(subject)
                .claim("username", username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate token with a custom TTL in milliseconds. Useful for sliding renewals.
     */
    public String generateTokenWithTtl(String subject, String username, Set<String> roles, long ttlMs) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlMs);
        return Jwts.builder()
                .setSubject(subject)
                .claim("username", username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
    }

    public Authentication buildAuthentication(String token) {
        Jws<Claims> jws = parseClaims(token);
        Claims claims = jws.getBody();
        String subject = claims.getSubject();
        @SuppressWarnings("unchecked")
        Collection<String> roles = (Collection<String>) claims.get("roles", Collection.class);
        List<GrantedAuthority> authorities = roles == null ? List.of() : roles.stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(subject, null, authorities);
    }

    public long getSlidingExpirationMs() {
        return slidingExpirationMs;
    }
}
