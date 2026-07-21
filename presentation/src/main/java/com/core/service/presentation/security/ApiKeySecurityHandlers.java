package com.core.service.presentation.security;

import com.core.service.presentation.response.ErrorResponse;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * Renders a consistent JSON {@link ErrorResponse} for authentication and authorization failures,
 * replacing Spring Security's default HTML / basic-auth responses.
 */
public class ApiKeySecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        writeError(request, response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized",
                "Missing or invalid API key.");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        writeError(request, response, HttpServletResponse.SC_FORBIDDEN, "Forbidden",
                "You are not allowed to access this resource.");
    }

    private void writeError(HttpServletRequest request, HttpServletResponse response,
                            int status, String title, String detail) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ErrorResponse error = ErrorResponse.of(status, title, detail, request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
