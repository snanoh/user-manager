package com.user.manage.dto.response;

import com.user.manage.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserInfoResponse {
    private Long id;
    private String email;
    private String role;
    private LocalDateTime createdAt;

    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
