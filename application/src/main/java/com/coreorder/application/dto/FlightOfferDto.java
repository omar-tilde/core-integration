package com.coreorder.application.dto;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO representing a flight offer for presentation to the API consumer.
 * Provider-independent — the consumer never sees provider-specific details.
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
            Duration totalDuration,
            int numberOfStops,
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
