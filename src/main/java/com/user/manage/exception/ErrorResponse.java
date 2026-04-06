package com.user.manage.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private String error;
    private String message;
    private LocalDateTime timestamp;

    public static ErrorResponse of(String error, String message) {
        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
