package com.user.manage.controller;

import com.user.manage.dto.request.LoginRequest;
import com.user.manage.dto.request.SignUpRequest;
import com.user.manage.dto.request.TokenRefreshRequest;
import com.user.manage.dto.response.TokenResponse;
import com.user.manage.security.CustomUserDetails;
import com.user.manage.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signUp(@Valid @RequestBody SignUpRequest request) {
        authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.logout(userDetails.getUser().getId());
        return ResponseEntity.ok(Map.of("message", "로그아웃되었습니다."));
    }
}
