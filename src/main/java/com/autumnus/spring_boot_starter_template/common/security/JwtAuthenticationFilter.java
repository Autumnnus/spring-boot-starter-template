package com.autumnus.spring_boot_starter_template.common.security;

import com.autumnus.spring_boot_starter_template.common.context.RequestContextHolder;
import com.autumnus.spring_boot_starter_template.modules.auth.service.DatabaseUserDetailsService;
import com.autumnus.spring_boot_starter_template.modules.auth.service.JwtService;
import com.autumnus.spring_boot_starter_template.common.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final DatabaseUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, DatabaseUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
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
                final Claims claims = jwtService.extractClaims(token);
                final List<SimpleGrantedAuthority> authorities = Optional.ofNullable(claims.get("roles", List.class))
                        .orElseGet(List::of)
                        .stream()
                        .map(Object::toString)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                final UserDetails principal = Optional.ofNullable(claims.get("uid", Number.class))
                        .map(Number::longValue)
                        .map(userDetailsService::loadUserById)
                        .orElseGet(() -> new org.springframework.security.core.userdetails.User(
                                claims.getSubject(),
                                "",
                                authorities
                        ));
                final var authentication = new UsernamePasswordAuthenticationToken(principal, token, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                final String userId = principal instanceof UserPrincipal userPrincipal
                        ? userPrincipal.getUserUuid().toString()
                        : authentication.getName();
                RequestContextHolder.getContext().setUserId(userId);
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
