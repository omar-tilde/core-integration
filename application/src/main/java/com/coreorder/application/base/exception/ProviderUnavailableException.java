package com.coreorder.application.base.exception;

/**
 * Application exception thrown when a specific provider is reachable = false (HTTP 503).
 */
public class ProviderUnavailableException extends ApplicationException {

    public ProviderUnavailableException(String message) {
        super(message);
    }
}
