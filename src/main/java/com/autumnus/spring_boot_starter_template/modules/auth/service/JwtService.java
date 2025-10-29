package com.autumnus.spring_boot_starter_template.modules.auth.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateAccessToken(UserDetails userDetails);

    String generateRefreshToken(Long userId);

    boolean validateToken(String token);

    Long extractUserId(String token);

    Claims extractClaims(String token);
}
