package com.coreorder.application.base.exception;

/**
 * Application exception thrown when zero providers are available to handle a request (HTTP 503).
 */
public class NoProviderAvailableException extends ApplicationException {

    public NoProviderAvailableException(String message) {
        super(message);
    }
}
