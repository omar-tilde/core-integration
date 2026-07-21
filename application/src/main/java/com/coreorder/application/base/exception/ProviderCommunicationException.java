package com.coreorder.application.base.exception;

/**
 * Application exception thrown when a provider communication error occurs (HTTP 502).
 */
public class ProviderCommunicationException extends ApplicationException {

    public ProviderCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
