package com.coreorder.application.command;

import java.time.LocalDate;
import java.util.List;

import com.coreorder.domain.model.valueobject.AirportCode;
import com.coreorder.domain.model.valueobject.CabinClass;
import com.coreorder.domain.model.valueobject.TripType;

/**
 * Command for searching flights.
 * Immutable — created once and passed to the use case.
 */
public record SearchFlightsCommand(
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
        List<String> preferredAirlines,
        String preferredProvider
) {

    public SearchFlightsCommand {
        if (preferredProvider != null && preferredProvider.isBlank()) {
            preferredProvider = null; // treat blank as no preference
        }
        preferredAirlines = preferredAirlines == null ? List.of() : List.copyOf(preferredAirlines);
    }
}
