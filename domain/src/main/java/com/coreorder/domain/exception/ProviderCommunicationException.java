package com.coreorder.domain.exception;

/**
 * Thrown by infrastructure adapters when communication with a provider fails.
 */
public class ProviderCommunicationException extends DomainException {

    public ProviderCommunicationException(String message) {
        super(message);
    }

    public ProviderCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
