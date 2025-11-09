package com.autumnus.spring_boot_starter_template.common.security;

import com.autumnus.spring_boot_starter_template.common.context.RequestContextHolder;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            final String token = bearerToken.substring(7);
            try {
                final var authentication = tokenProvider.toAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                if (authentication.getPrincipal() instanceof UserPrincipal principal) {
                    RequestContextHolder.getContext().setUserId(String.valueOf(principal.getUserId()));
                    RequestContextHolder.getContext().setClientId(principal.getEmail());
                } else {
                    RequestContextHolder.getContext().setUserId(authentication.getName());
                }
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
                throw new UnauthorizedException("Invalid or expired token");
            }
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
