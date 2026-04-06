package com.user.manage.service;

import com.user.manage.entity.RefreshToken;
import com.user.manage.entity.User;
import com.user.manage.exception.TokenException;
import com.user.manage.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DbRefreshTokenService implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public RefreshToken verifyRefreshToken(String tokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(TokenException::notFound);

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw TokenException.expired();
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
