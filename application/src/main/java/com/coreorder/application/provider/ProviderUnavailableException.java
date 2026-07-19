package com.coreorder.application.provider;

/**
 * Thrown when a specific provider is known but currently unavailable (down, unreachable, etc.).
 */
public class ProviderUnavailableException extends RuntimeException {

    public ProviderUnavailableException(String message) {
        super(message);
    }
}
