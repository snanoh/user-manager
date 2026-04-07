package com.user.manage.service.impl;

import com.user.manage.entity.RefreshToken;
import com.user.manage.entity.User;
import com.user.manage.exception.TokenException;
import com.user.manage.repository.UserRepository;
import com.user.manage.service.RefreshTokenService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Redis 전용 Refresh Token 관리 (선택 과제).
 * DB를 사용하지 않고 Redis만으로 토큰을 관리합니다.
 *
 * 저장 구조:
 *   refresh_token:{userId}       → UUID  (유저 → 토큰)
 *   refresh_token_lookup:{UUID}  → userId (토큰 → 유저, 역방향)
 */
@Primary
@Profile("!test")
@Service
@RequiredArgsConstructor
public class RedisRefreshTokenServiceImpl implements RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    private String userKey(Long userId) {
        return "refresh_token:" + userId;
    }

    private String lookupKey(String token) {
        return "refresh_token_lookup:" + token;
    }

    @Override
    public RefreshToken createRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        Duration ttl = Duration.ofMillis(refreshTokenExpiryMs);

        // 기존 토큰 제거 (rotation)
        String oldToken = redisTemplate.opsForValue().get(userKey(user.getId()));
        if (oldToken != null) {
            redisTemplate.delete(lookupKey(oldToken));
        }

        redisTemplate.opsForValue().set(userKey(user.getId()), tokenValue, ttl);
        redisTemplate.opsForValue().set(lookupKey(tokenValue), String.valueOf(user.getId()), ttl);

        return RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plus(ttl))
                .build();
    }

    @Override
    public RefreshToken verifyRefreshToken(String tokenValue) {
        String userId = redisTemplate.opsForValue().get(lookupKey(tokenValue));
        if (userId == null) {
            throw TokenException.expired();
        }

        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        return RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
                .build();
    }

    @Override
    public void deleteByUser(User user) {
        String tokenValue = redisTemplate.opsForValue().get(userKey(user.getId()));
        if (tokenValue != null) {
            redisTemplate.delete(lookupKey(tokenValue));
        }
        redisTemplate.delete(userKey(user.getId()));
    }
}
