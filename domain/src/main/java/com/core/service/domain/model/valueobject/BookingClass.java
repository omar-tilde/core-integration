package com.core.service.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Booking class (single-letter code, e.g., "Y", "J", "F").
 * Immutable value object.
 */
public record BookingClass(String code) {

    private static final Pattern BOOKING_CLASS_PATTERN = Pattern.compile("[A-Z]");

    public BookingClass {
        Objects.requireNonNull(code, "Booking class must not be null");
        if (!BOOKING_CLASS_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException(
                    "Invalid booking class: '%s'. Must be a single uppercase letter.".formatted(code));
        }
    }

    public static BookingClass of(String code) {
        return new BookingClass(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
