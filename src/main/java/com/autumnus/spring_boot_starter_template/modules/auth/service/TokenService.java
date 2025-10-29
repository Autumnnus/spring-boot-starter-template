package com.autumnus.spring_boot_starter_template.modules.auth.service;

import com.autumnus.spring_boot_starter_template.modules.users.entity.RefreshToken;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;

public interface TokenService {

    RefreshToken createRefreshToken(User user, String deviceInfo, String ipAddress);

    RefreshToken validateRefreshToken(String token);

    void revokeToken(String token);

    void revokeAllUserTokens(Long userId);

    void cleanExpiredTokens();
}
