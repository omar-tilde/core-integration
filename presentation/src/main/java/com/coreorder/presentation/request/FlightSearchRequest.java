package com.coreorder.presentation.request;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * REST request model for flight search.
 * Validated via Jakarta Validation before reaching the application layer.
 * <p>
 * Integer counters (rather than primitive {@code int}) so that Jackson 3 can
 * tolerate a missing field and the compact constructor can apply a default.
 */
public record FlightSearchRequest(

        @NotBlank(message = "Trip type is required")
        String tripType,

        @NotBlank(message = "Origin is required")
        @Size(min = 3, max = 3, message = "Origin must be a 3-letter IATA code")
        String origin,

        @NotBlank(message = "Destination is required")
        @Size(min = 3, max = 3, message = "Destination must be a 3-letter IATA code")
        String destination,

        @NotNull(message = "Departure date is required")
        String departureDate,

        String returnDate,

        @Min(value = 1, message = "At least one adult passenger is required")
        @Max(value = 9, message = "Maximum 9 adult passengers allowed")
        Integer adultCount,

        @Min(value = 0, message = "Child count must not be negative")
        @Max(value = 9, message = "Maximum 9 child passengers allowed")
        Integer childCount,

        @Min(value = 0, message = "Infant count must not be negative")
        @Max(value = 9, message = "Maximum 9 infant passengers allowed")
        Integer infantCount,

        String cabinClass,

        @Min(value = 1, message = "Max results must be at least 1")
        @Max(value = 200, message = "Max results must not exceed 200")
        Integer maxResults,

        List<String> preferredAirlines,

        String preferredProvider
) {
    public FlightSearchRequest {
        int adults = (adultCount == null) ? 1 : adultCount;
        int children = (childCount == null) ? 0 : childCount;
        int infants = (infantCount == null) ? 0 : infantCount;
        int max = (maxResults == null) ? 50 : maxResults;
        String cabin = (cabinClass == null || cabinClass.isBlank()) ? "ECONOMY" : cabinClass;
        List<String> airlines = preferredAirlines == null ? List.of() : preferredAirlines;

        adultCount = adults;
        childCount = children;
        infantCount = infants;
        maxResults = max;
        cabinClass = cabin;
        preferredAirlines = airlines;
    }
}
