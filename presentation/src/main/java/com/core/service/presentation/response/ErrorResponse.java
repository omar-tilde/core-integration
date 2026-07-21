package com.core.service.presentation.response;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response following RFC 7807 (Problem Details for HTTP APIs).
 */
public record ErrorResponse(
        String type,
        String title,
        int status,
        String detail,
        String path,
        Instant timestamp,
        List<FieldError> errors
) {

    public record FieldError(
            String field,
            String message
    ) {}

    public static ErrorResponse of(int status, String title, String detail, String path) {
        return new ErrorResponse(
                "about:blank",
                title,
                status,
                detail,
                path,
                Instant.now(),
                List.of()
        );
    }

    public static ErrorResponse withFieldErrors(int status, String title, String detail, String path,
                                                 List<FieldError> fieldErrors) {
        return new ErrorResponse(
                "about:blank",
                title,
                status,
                detail,
                path,
                Instant.now(),
                fieldErrors
        );
    }
}
