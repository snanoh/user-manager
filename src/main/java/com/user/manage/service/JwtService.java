package com.user.manage.service;

import com.user.manage.config.JwtConfig;
import com.user.manage.entity.User;
import com.user.manage.exception.TokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfig jwtConfig;

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getAccessTokenExpiryMs()))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw TokenException.expired();
        } catch (JwtException e) {
            throw TokenException.invalid();
        }
    }

    public Long extractUserId(String token) {
        return Long.parseLong(validateToken(token).getSubject());
    }

    public long getAccessTokenExpirySeconds() {
        return jwtConfig.getAccessTokenExpiryMs() / 1000;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
