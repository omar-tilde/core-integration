package com.coreorder.application.flightsearch;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for flight offers.
 */
public record FlightOfferDto(
        String offerId,
        String providerId,
        List<ItineraryDto> itineraries,
        PriceDto totalPrice,
        PriceDto basePrice,
        PriceDto taxAmount,
        String validatingCarrier,
        boolean refundable,
        int seatsAvailable
) {
    public record ItineraryDto(
            String origin,
            String destination,
            LocalDateTime departureTime,
            LocalDateTime arrivalTime,
            Duration duration,
            int stops,
            List<SegmentDto> segments
    ) {}

    public record SegmentDto(
            String flightNumber,
            String origin,
            String destination,
            LocalDateTime departureTime,
            LocalDateTime arrivalTime,
            Duration duration,
            String cabinClass,
            String bookingClass,
            int availableSeats,
            String equipmentCode,
            String aircraftType
    ) {}

    public record PriceDto(
            BigDecimal amount,
            String currency
    ) {}
}
