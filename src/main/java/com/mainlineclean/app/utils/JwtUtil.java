package com.mainlineclean.app.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    @Value("${client.origin}")
    private String CLIENT_ORIGIN;

    @Value("${spring.security.jwt.secret}")
    private String SECRET;

    private static final long VALIDITY = TimeUnit.DAYS.toMillis(30);

    public String generateToken() {
        Map<String, String> claims = new HashMap<>();
        claims.put("iss", CLIENT_ORIGIN);
        return Jwts.builder()
                .claims(claims)
                .subject("Admin User")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(VALIDITY)))
                .signWith(generateKey())
                .compact();
    }

    private SecretKey generateKey() {
        byte[] decodedKey = Base64.getDecoder().decode(SECRET);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    public Claims getClaims(String jwt) {
        return Jwts.parser()
                .verifyWith(generateKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }
}
