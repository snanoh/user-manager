package com.user.manage.controller;

import com.user.manage.dto.response.UserInfoResponse;
import com.user.manage.security.CustomUserDetails;
import com.user.manage.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserInfo(userDetails.getUser().getId()));
    }
}
