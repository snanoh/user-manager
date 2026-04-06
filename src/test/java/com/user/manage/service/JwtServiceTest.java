package com.user.manage.service;

import com.user.manage.config.JwtConfig;
import com.user.manage.entity.Role;
import com.user.manage.entity.User;
import com.user.manage.exception.TokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private JwtConfig jwtConfig;

    // 256-bit Base64 secret for testing
    private static final String TEST_SECRET =
            "dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLWhzMjU2";

    @BeforeEach
    void setUp() throws Exception {
        jwtConfig = new JwtConfig();
        setField(jwtConfig, "secret", TEST_SECRET);
        setField(jwtConfig, "accessTokenExpiryMs", 900_000L);
        setField(jwtConfig, "refreshTokenExpiryMs", 604_800_000L);

        jwtService = new JwtService(jwtConfig);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private User testUser() {
        try {
            User user = User.builder()
                    .email("test@example.com")
                    .password("hashedPw")
                    .build();
            setField(user, "id", 1L);
            setField(user, "role", Role.USER);
            return user;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("① 정상적인 Access Token 생성 및 userId 파싱")
    void generateAccessToken_shouldContainCorrectUserId() {
        User user = testUser();
        String token = jwtService.generateAccessToken(user);

        Long userId = jwtService.extractUserId(token);

        assertThat(userId).isEqualTo(1L);
    }

    @Test
    @DisplayName("② 만료된 토큰 검증 시 TokenException 발생")
    void validateToken_shouldThrowTokenException_whenExpired() throws Exception {
        // 만료 시간을 -1ms로 설정
        setField(jwtConfig, "accessTokenExpiryMs", -1L);
        User user = testUser();
        String expiredToken = jwtService.generateAccessToken(user);

        assertThatThrownBy(() -> jwtService.validateToken(expiredToken))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining("만료");
    }

    @Test
    @DisplayName("③ 변조된 토큰 검증 시 TokenException 발생")
    void validateToken_shouldThrowTokenException_whenTampered() {
        User user = testUser();
        String token = jwtService.generateAccessToken(user);
        String tamperedToken = token + "tampered";

        assertThatThrownBy(() -> jwtService.validateToken(tamperedToken))
                .isInstanceOf(TokenException.class);
    }
}
