package com.coreorder.infrastructure.travelport.base;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Travelport provider.
 * Binds to {@code providers.travelport.*} in application.yml.
 */
@ConfigurationProperties(prefix = "providers.travelport")
public record TravelportProperties(
        boolean enabled,
        String baseUrl,
        String branchCode,
        String username,
        String password,
        int connectionTimeoutMs,
        int readTimeoutMs
) {

    public TravelportProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.travelport.com/universal";
        }
        if (connectionTimeoutMs <= 0) {
            connectionTimeoutMs = 5000;
        }
        if (readTimeoutMs <= 0) {
            readTimeoutMs = 30000;
        }
    }
}
