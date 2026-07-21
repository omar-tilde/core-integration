package com.coreorder.domain.exception;

/**
 * Thrown by strategy registry when no active provider is available.
 */
public class NoProviderAvailableException extends DomainException {

    public NoProviderAvailableException(String message) {
        super(message);
    }
}
