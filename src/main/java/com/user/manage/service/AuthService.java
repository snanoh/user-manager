package com.user.manage.service;

import com.user.manage.dto.request.LoginRequest;
import com.user.manage.dto.request.SignUpRequest;
import com.user.manage.dto.response.TokenResponse;
import com.user.manage.entity.RefreshToken;
import com.user.manage.entity.User;
import com.user.manage.exception.DuplicateEmailException;
import com.user.manage.exception.InvalidCredentialsException;
import com.user.manage.repository.UserRepository;
import com.user.manage.security.CustomUserDetails;
import com.user.manage.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsServiceImpl userDetailsService;

    @Transactional
    public void signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return TokenResponse.of(accessToken, refreshToken.getToken(), jwtService.getAccessTokenExpirySeconds());
    }

    @Transactional
    public TokenResponse refresh(String refreshTokenValue) {
        RefreshToken oldToken = refreshTokenService.verifyRefreshToken(refreshTokenValue);
        User user = oldToken.getUser();

        // Rotation: 기존 삭제 후 신규 발급
        refreshTokenService.deleteByUser(user);

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return TokenResponse.of(accessToken, newRefreshToken.getToken(), jwtService.getAccessTokenExpirySeconds());
    }

    @Transactional
    public void logout(Long userId) {
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserById(userId);
        refreshTokenService.deleteByUser(userDetails.getUser());
    }
}
