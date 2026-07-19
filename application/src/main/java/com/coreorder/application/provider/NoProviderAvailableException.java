package com.coreorder.application.provider;

/**
 * Thrown when no provider is currently available to handle a request.
 */
public class NoProviderAvailableException extends RuntimeException {

    public NoProviderAvailableException(String message) {
        super(message);
    }
}
