package com.coreorder.domain.model.valueobject;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Search criteria for flight search operations.
 * Immutable value object — captures what the consumer wants, independent of any provider.
 */
public record FlightSearchCriteria(
        TripType tripType,
        AirportCode origin,
        AirportCode destination,
        LocalDate departureDate,
        LocalDate returnDate,
        int adultCount,
        int childCount,
        int infantCount,
        CabinClass cabinClass,
        int maxResults,
        List<String> preferredAirlines
) {

    public FlightSearchCriteria {
        Objects.requireNonNull(tripType, "Trip type must not be null");
        Objects.requireNonNull(origin, "Origin must not be null");
        Objects.requireNonNull(destination, "Destination must not be null");
        Objects.requireNonNull(departureDate, "Departure date must not be null");
        Objects.requireNonNull(cabinClass, "Cabin class must not be null");

        if (adultCount < 1) {
            throw new IllegalArgumentException("At least one adult passenger is required");
        }
        if (childCount < 0 || infantCount < 0) {
            throw new IllegalArgumentException("Passenger counts must not be negative");
        }
        if (maxResults <= 0) {
            throw new IllegalArgumentException("Max results must be positive");
        }
        if (tripType == TripType.ROUND_TRIP && returnDate == null) {
            throw new IllegalArgumentException("Return date is required for round-trip searches");
        }
        if (returnDate != null && returnDate.isBefore(departureDate)) {
            throw new IllegalArgumentException("Return date must not be before departure date");
        }

        // Defensive copy for list
        preferredAirlines = preferredAirlines == null ? List.of() : List.copyOf(preferredAirlines);
    }

    /**
     * Total passenger count across all types.
     */
    public int totalPassengers() {
        return adultCount + childCount + infantCount;
    }

    public boolean isOneWay() {
        return tripType == TripType.ONE_WAY;
    }

    public boolean isRoundTrip() {
        return tripType == TripType.ROUND_TRIP;
    }
}
