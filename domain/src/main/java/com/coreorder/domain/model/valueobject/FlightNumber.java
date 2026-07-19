package com.coreorder.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Flight number combining airline code and a numeric flight number (e.g., "AF1234").
 * Immutable value object.
 */
public record FlightNumber(AirlineCode airlineCode, String number) {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d{1,4}[A-Z]?");

    public FlightNumber {
        Objects.requireNonNull(airlineCode, "Airline code must not be null");
        Objects.requireNonNull(number, "Flight number must not be null");
        if (!NUMBER_PATTERN.matcher(number).matches()) {
            throw new IllegalArgumentException(
                    "Invalid flight number: '%s'. Must be 1-4 digits optionally followed by one letter.".formatted(number));
        }
    }

    public static FlightNumber of(AirlineCode airlineCode, String number) {
        return new FlightNumber(airlineCode, number);
    }

    public static FlightNumber parse(String marketingCarrier) {
        Objects.requireNonNull(marketingCarrier, "Marketing carrier must not be null");
        if (marketingCarrier.length() < 3) {
            throw new IllegalArgumentException("Invalid marketing carrier format: '%s'".formatted(marketingCarrier));
        }
        AirlineCode airlineCode = AirlineCode.of(marketingCarrier.substring(0, 2));
        String number = marketingCarrier.substring(2);
        return new FlightNumber(airlineCode, number);
    }

    public String toMarketingString() {
        return airlineCode().code() + number;
    }

    @Override
    public String toString() {
        return toMarketingString();
    }
}
