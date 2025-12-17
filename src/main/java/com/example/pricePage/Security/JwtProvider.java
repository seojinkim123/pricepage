package com.example.pricePage.Security;

import com.example.pricePage.Entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessMs;
    private final long refreshMs;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expire-ms}") long accessMs,
            @Value("${jwt.refresh-token-expire-ms}") long refreshMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessMs = accessMs;
        this.refreshMs = refreshMs;
    }

    public String createAccessToken(Long userId, Role role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role.name())
                .claim("type", "access")
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + accessMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("type", "refresh")
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }

    public Role getRole(String token) {
        return Role.valueOf(parse(token).get("role", String.class));
    }

    public String getType(String token) {
        return parse(token).get("type", String.class);
    }

    public long getAccessExpireSeconds() {
        return accessMs / 1000L;
    }
}