package com.core.service.domain.model.enums;

/**
 * Strongly typed enumeration for supported aviation distribution providers.
 */
public enum ProviderType {
    AMADEUS,
    TRAVELPORT,
    SABRE;

    /**
     * Parse a string into a {@link ProviderType} case-insensitively.
     *
     * @param code provider identifier code (e.g. "AMADEUS", "Travelport")
     * @return the matching {@link ProviderType}, or null if code is null/blank
     * @throws IllegalArgumentException if code is non-blank but matches no enum value
     */
    public static ProviderType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (ProviderType type : values()) {
            if (type.name().equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown provider code: '%s'".formatted(code));
    }
}
