package com.core.service.application.flightsearch;

import com.core.service.application.base.exception.InvalidCommandException;

import java.time.LocalDate;
import java.util.List;

/**
 * Command input object for flight search (uses standard primitives/types).
 */
public record SearchFlightsCommand(
        String tripType,
        String origin,
        String destination,
        LocalDate departureDate,
        LocalDate returnDate,
        int adultCount,
        int childCount,
        int infantCount,
        String cabinClass,
        int maxResults,
        List<String> preferredAirlines,
        String preferredProvider
) {
    public SearchFlightsCommand {
        if (tripType == null || tripType.isBlank()) {
            throw new InvalidCommandException("Trip type must not be null or blank");
        }
        if (origin == null || origin.isBlank()) {
            throw new InvalidCommandException("Origin airport must not be null or blank");
        }
        if (destination == null || destination.isBlank()) {
            throw new InvalidCommandException("Destination airport must not be null or blank");
        }
        if (origin.trim().equalsIgnoreCase(destination.trim())) {
            throw new InvalidCommandException("Origin and destination airports must be different");
        }
        if (departureDate == null) {
            throw new InvalidCommandException("Departure date must not be null");
        }
        if (departureDate.isBefore(LocalDate.now())) {
            throw new InvalidCommandException("Departure date cannot be in the past");
        }
        if ("ROUND_TRIP".equalsIgnoreCase(tripType.trim())) {
            if (returnDate == null) {
                throw new InvalidCommandException("Return date is required for round-trip searches");
            }
            if (returnDate.isBefore(departureDate)) {
                throw new InvalidCommandException("Return date cannot be before departure date");
            }
        }
        if (adultCount < 1) {
            throw new InvalidCommandException("At least one adult passenger is required");
        }
        if (childCount < 0 || infantCount < 0) {
            throw new InvalidCommandException("Passenger counts cannot be negative");
        }
        if (infantCount > adultCount) {
            throw new InvalidCommandException("Infant count cannot exceed adult count");
        }
        if (cabinClass == null || cabinClass.isBlank()) {
            cabinClass = "ECONOMY";
        }
        if (maxResults <= 0) {
            maxResults = 50;
        }
        if (preferredAirlines == null) {
            preferredAirlines = List.of();
        }
    }
}
