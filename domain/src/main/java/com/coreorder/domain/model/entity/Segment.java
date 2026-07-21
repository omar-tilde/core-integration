package com.coreorder.domain.model.entity;

import com.coreorder.domain.model.valueobject.AirportCode;
import com.coreorder.domain.model.valueobject.BookingClass;
import com.coreorder.domain.model.valueobject.CabinClass;
import com.coreorder.domain.model.valueobject.FlightNumber;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A single flight segment (one takeoff to one landing, one flight number).
 * Immutable domain entity.
 */
public record Segment(
        FlightNumber flightNumber,
        AirportCode origin,
        AirportCode destination,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        CabinClass cabinClass,
        BookingClass bookingClass,
        int availableSeats,
        AircraftInfo aircraftInfo
) {

    public Segment {
        Objects.requireNonNull(flightNumber, "Flight number must not be null");
        Objects.requireNonNull(origin, "Origin must not be null");
        Objects.requireNonNull(destination, "Destination must not be null");
        Objects.requireNonNull(departureTime, "Departure time must not be null");
        Objects.requireNonNull(arrivalTime, "Arrival time must not be null");
        Objects.requireNonNull(cabinClass, "Cabin class must not be null");
        Objects.requireNonNull(bookingClass, "Booking class must not be null");
        Objects.requireNonNull(aircraftInfo, "Aircraft info must not be null");

        if (!arrivalTime.isAfter(departureTime)) {
            throw new IllegalArgumentException("Arrival time must be after departure time");
        }
        if (availableSeats < 0) {
            throw new IllegalArgumentException("Available seats must not be negative");
        }
    }

    public Duration duration() {
        return Duration.between(departureTime, arrivalTime);
    }

    /**
     * Basic aircraft information associated with a segment.
     */
    public record AircraftInfo(String equipmentCode, String aircraftType) {
        public AircraftInfo {
            Objects.requireNonNull(equipmentCode, "Equipment code must not be null");
            Objects.requireNonNull(aircraftType, "Aircraft type must not be null");
        }

        public static AircraftInfo of(String equipmentCode, String aircraftType) {
            return new AircraftInfo(equipmentCode, aircraftType);
        }
    }
}
