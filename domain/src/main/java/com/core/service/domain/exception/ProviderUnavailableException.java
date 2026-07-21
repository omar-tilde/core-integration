package com.core.service.domain.exception;

/**
 * Thrown by strategy registry when a provider is registered but unavailable.
 */
public class ProviderUnavailableException extends DomainException {

    public ProviderUnavailableException(String message) {
        super(message);
    }
}
