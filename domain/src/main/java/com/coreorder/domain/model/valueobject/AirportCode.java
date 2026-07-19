package com.coreorder.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * IATA airport code (3-letter uppercase code, e.g., "CDG", "JFK").
 * Immutable value object.
 */
public record AirportCode(String code) {

    private static final Pattern IATA_PATTERN = Pattern.compile("[A-Z]{3}");

    public AirportCode {
        Objects.requireNonNull(code, "Airport code must not be null");
        if (!IATA_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException(
                    "Invalid IATA airport code: '%s'. Must be exactly 3 uppercase letters.".formatted(code));
        }
    }

    /**
     * Convenience factory for string-based construction.
     */
    public static AirportCode of(String code) {
        return new AirportCode(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
