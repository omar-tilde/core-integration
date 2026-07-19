package com.coreorder.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * IATA airline code (2-character alphanumeric code, e.g., "AF", "LH", "BA").
 * Immutable value object.
 */
public record AirlineCode(String code) {

    private static final Pattern IATA_AIRLINE_PATTERN = Pattern.compile("[A-Z0-9]{2}");

    public AirlineCode {
        Objects.requireNonNull(code, "Airline code must not be null");
        if (!IATA_AIRLINE_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException(
                    "Invalid IATA airline code: '%s'. Must be exactly 2 uppercase alphanumeric characters.".formatted(code));
        }
    }

    public static AirlineCode of(String code) {
        return new AirlineCode(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
