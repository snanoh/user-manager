package com.user.manage.service;

import com.user.manage.dto.request.LoginRequest;
import com.user.manage.dto.request.SignUpRequest;
import com.user.manage.dto.response.TokenResponse;
import com.user.manage.entity.RefreshToken;
import com.user.manage.entity.Role;
import com.user.manage.entity.User;
import com.user.manage.exception.DuplicateEmailException;
import com.user.manage.exception.InvalidCredentialsException;
import com.user.manage.repository.UserRepository;
import com.user.manage.security.CustomUserDetails;
import com.user.manage.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock RefreshTokenService refreshTokenService;
    @Mock UserDetailsServiceImpl userDetailsService;

    @InjectMocks AuthService authService;

    private User buildUser(Long id, String email) throws Exception {
        User user = User.builder().email(email).password("$2a$10$hashed").build();
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
        Field roleField = User.class.getDeclaredField("role");
        roleField.setAccessible(true);
        roleField.set(user, Role.USER);
        return user;
    }

    @Test
    @DisplayName("④ 회원가입 시 비밀번호가 해시 처리되어 저장됨")
    void signUp_shouldSaveHashedPassword() {
        SignUpRequest request = mockSignUpRequest("test@example.com", "password123");
        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("$2a$10$hashed");

        authService.signUp(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should().save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("$2a$10$hashed");
    }

    @Test
    @DisplayName("⑤ 중복 이메일 회원가입 시 DuplicateEmailException 발생")
    void signUp_shouldThrowDuplicateEmailException_whenEmailExists() {
        SignUpRequest request = mockSignUpRequest("dup@example.com", "password123");
        given(userRepository.existsByEmail("dup@example.com")).willReturn(true);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    @DisplayName("⑥ 올바른 자격증명으로 로그인 시 TokenResponse 반환")
    void login_shouldReturnTokenResponse_onValidCredentials() throws Exception {
        User user = buildUser(1L, "test@example.com");
        LoginRequest request = mockLoginRequest("test@example.com", "password123");

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", user.getPassword())).willReturn(true);
        given(jwtService.generateAccessToken(user)).willReturn("access.token.value");
        given(jwtService.getAccessTokenExpirySeconds()).willReturn(900L);

        RefreshToken rt = RefreshToken.builder()
                .user(user).token("refresh-uuid")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        given(refreshTokenService.createRefreshToken(user)).willReturn(rt);

        TokenResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access.token.value");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-uuid");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("⑦ 잘못된 비밀번호 로그인 시 InvalidCredentialsException 발생")
    void login_shouldThrowInvalidCredentials_onBadPassword() throws Exception {
        User user = buildUser(1L, "test@example.com");
        LoginRequest request = mockLoginRequest("test@example.com", "wrongPassword");

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongPassword", user.getPassword())).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    // ---- helpers ----

    private SignUpRequest mockSignUpRequest(String email, String password) {
        try {
            SignUpRequest req = new SignUpRequest();
            Field e = SignUpRequest.class.getDeclaredField("email");
            e.setAccessible(true); e.set(req, email);
            Field p = SignUpRequest.class.getDeclaredField("password");
            p.setAccessible(true); p.set(req, password);
            return req;
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }

    private LoginRequest mockLoginRequest(String email, String password) {
        try {
            LoginRequest req = new LoginRequest();
            Field e = LoginRequest.class.getDeclaredField("email");
            e.setAccessible(true); e.set(req, email);
            Field p = LoginRequest.class.getDeclaredField("password");
            p.setAccessible(true); p.set(req, password);
            return req;
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }
}
