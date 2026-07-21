package com.core.service.domain.model.valueobject;

/**
 * Cabin class for a flight segment.
 */
public enum CabinClass {
    ECONOMY,
    PREMIUM_ECONOMY,
    BUSINESS,
    FIRST;

    public static CabinClass fromString(String value) {
        for (CabinClass cabin : values()) {
            if (cabin.name().equalsIgnoreCase(value)) {
                return cabin;
            }
        }
        throw new IllegalArgumentException("Unknown cabin class: '%s'".formatted(value));
    }
}
