package com.user.manage.service;

import com.user.manage.entity.RefreshToken;
import com.user.manage.entity.User;
import com.user.manage.exception.TokenException;
import com.user.manage.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Redis를 이용한 Refresh Token 관리 (선택 과제).
 * Redis에 저장 (key: refresh_token:{userId}, TTL 자동 만료) + DB에도 동기화.
 * @Primary 로 기본 구현체로 사용.
 */
@Primary
@Profile("!test")
@Service
@RequiredArgsConstructor
public class RedisRefreshTokenService implements RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    private String redisKey(Long userId) {
        return "refresh_token:" + userId;
    }

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        Duration ttl = Duration.ofMillis(refreshTokenExpiryMs);

        redisTemplate.opsForValue().set(redisKey(user.getId()), tokenValue, ttl);

        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plus(ttl))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyRefreshToken(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(TokenException::notFound);

        String redisValue = redisTemplate.opsForValue().get(redisKey(token.getUser().getId()));
        if (redisValue == null || !redisValue.equals(tokenValue)) {
            refreshTokenRepository.delete(token);
            throw TokenException.expired();
        }

        return token;
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        redisTemplate.delete(redisKey(user.getId()));
        refreshTokenRepository.deleteByUser(user);
    }
}
