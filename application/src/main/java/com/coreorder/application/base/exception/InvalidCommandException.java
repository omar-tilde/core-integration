package com.coreorder.application.base.exception;

/**
 * Application exception thrown when a command/query payload fails validation (HTTP 400).
 */
public class InvalidCommandException extends ApplicationException {

    public InvalidCommandException(String message) {
        super(message);
    }
}
