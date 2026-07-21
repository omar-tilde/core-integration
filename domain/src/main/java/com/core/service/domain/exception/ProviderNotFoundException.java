package com.core.service.domain.exception;

/**
 * Thrown by strategy registry when a requested provider is not found.
 */
public class ProviderNotFoundException extends DomainException {

    public ProviderNotFoundException(String message) {
        super(message);
    }
}
