package com.iapp.iapp_messenger.controllers.services;

import com.iapp.iapp_messenger.dao.hibernate.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private final byte[] secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secretKey = secret.getBytes();
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getLogin())
                .claim("id", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(Keys.hmacShaKeyFor(secretKey))
                .compact();
    }

    public String extractLogin(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isValid(String token, User user) {
        String login = extractLogin(token);
        return login.equals(user.getLogin()) && !isExpired(token);
    }

    private boolean isExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}