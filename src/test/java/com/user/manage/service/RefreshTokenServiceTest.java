package com.user.manage.service;

import com.user.manage.entity.RefreshToken;
import com.user.manage.entity.User;
import com.user.manage.exception.TokenException;
import com.user.manage.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock RefreshTokenRepository refreshTokenRepository;

    @InjectMocks DbRefreshTokenService refreshTokenService;

    private User testUser() {
        return User.builder().email("test@example.com").password("hash").build();
    }

    @Test
    @DisplayName("⑧ 만료된 Refresh Token 검증 시 TokenException 발생")
    void verifyRefreshToken_shouldThrowTokenException_whenExpired() {
        User user = testUser();
        RefreshToken expired = RefreshToken.builder()
                .user(user)
                .token("expired-token")
                .expiresAt(LocalDateTime.now().minusSeconds(1))
                .build();

        given(refreshTokenRepository.findByToken("expired-token")).willReturn(Optional.of(expired));

        assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken("expired-token"))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining("만료");

        then(refreshTokenRepository).should().delete(expired);
    }

    @Test
    @DisplayName("⑨ 유효한 Refresh Token 검증 시 토큰 반환")
    void verifyRefreshToken_shouldReturnToken_whenValid() {
        User user = testUser();
        RefreshToken valid = RefreshToken.builder()
                .user(user)
                .token("valid-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        given(refreshTokenRepository.findByToken("valid-token")).willReturn(Optional.of(valid));

        RefreshToken result = refreshTokenService.verifyRefreshToken("valid-token");

        assertThat(result.getToken()).isEqualTo("valid-token");
        then(refreshTokenRepository).should(never()).delete(any());
    }
}
