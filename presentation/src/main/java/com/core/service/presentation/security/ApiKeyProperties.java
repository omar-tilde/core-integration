package com.core.service.presentation.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for API-key authentication.
 * <p>
 * Bound from the {@code api.*} namespace. The secret is read from {@code api.key}, which is also
 * satisfiable via the {@code API_KEY} environment variable. The header that carries the key is
 * configurable through {@code api.header-name} (env {@code API_HEADER_NAME}).
 */
@ConfigurationProperties(prefix = "api")
public record ApiKeyProperties(
        String key,
        String headerName
) {
    public ApiKeyProperties {
        if (headerName == null || headerName.isBlank()) {
            headerName = "X-API-KEY";
        }
    }
}
