package com.coreorder.application.provider;

/**
 * Thrown when communication with an external provider fails.
 * <p>
 * Lives in the application layer so that the presentation layer can handle it
 * without taking a dependency on the infrastructure layer.
 */
public class ProviderCommunicationException extends RuntimeException {

    public ProviderCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProviderCommunicationException(String message) {
        super(message);
    }
}
