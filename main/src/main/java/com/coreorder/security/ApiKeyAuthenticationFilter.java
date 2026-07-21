package com.coreorder.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates HTTP requests using a shared API key presented in a configurable request header.
 * <p>
 * When a valid key is supplied the request is marked as authenticated; requests without a valid key
 * are left unauthenticated and are subsequently rejected by the authorization rules defined in
 * {@link ApiKeySecurityConfig}. Public endpoints are exempted by those rules, not by this filter.
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyProperties properties;

    public ApiKeyAuthenticationFilter(ApiKeyProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String expectedKey = properties.key();
        if (expectedKey != null && !expectedKey.isBlank()) {
            String providedKey = request.getHeader(properties.headerName());
            if (expectedKey.equals(providedKey)) {
                var authentication = new UsernamePasswordAuthenticationToken(
                        "api-client", null, List.of(new SimpleGrantedAuthority("ROLE_API")));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
