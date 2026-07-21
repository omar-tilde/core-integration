package com.coreorder.infrastructure.amadeus.base;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Amadeus provider.
 * Binds to {@code providers.amadeus.*} in application.yml.
 */
@ConfigurationProperties(prefix = "providers.amadeus")
public record AmadeusProperties(
        boolean enabled,
        String baseUrl,
        String clientId,
        String clientSecret,
        int connectionTimeoutMs,
        int readTimeoutMs
) {

    public AmadeusProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.amadeus.com/v2";
        }
        if (connectionTimeoutMs <= 0) {
            connectionTimeoutMs = 5000;
        }
        if (readTimeoutMs <= 0) {
            readTimeoutMs = 30000;
        }
    }
}
