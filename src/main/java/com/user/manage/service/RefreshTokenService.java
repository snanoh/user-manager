package com.user.manage.service;

import com.user.manage.entity.RefreshToken;
import com.user.manage.entity.User;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);
    RefreshToken verifyRefreshToken(String tokenValue);
    void deleteByUser(User user);
}
