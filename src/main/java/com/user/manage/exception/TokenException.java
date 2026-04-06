package com.user.manage.exception;

public class TokenException extends RuntimeException {

    private final String errorCode;

    public TokenException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public static TokenException expired() {
        return new TokenException("TOKEN_EXPIRED", "토큰이 만료되었습니다.");
    }

    public static TokenException invalid() {
        return new TokenException("TOKEN_INVALID", "유효하지 않은 토큰입니다.");
    }

    public static TokenException notFound() {
        return new TokenException("TOKEN_NOT_FOUND", "Refresh Token을 찾을 수 없습니다.");
    }
}
