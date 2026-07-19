package com.coreorder.application.provider;

/**
 * Thrown when a requested provider ID does not match any registered provider.
 */
public class ProviderNotFoundException extends RuntimeException {

    public ProviderNotFoundException(String message) {
        super(message);
    }
}
