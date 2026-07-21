package com.coreorder.application.base.exception;

/**
 * Application exception thrown when a requested provider is not registered (HTTP 404).
 */
public class ProviderNotFoundException extends ApplicationException {

    public ProviderNotFoundException(String message) {
        super(message);
    }
}
