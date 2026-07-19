package com.coreorder.application.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO representing an order for presentation to the API consumer.
 */
public record OrderDto(
        String orderId,
        String providerOrderId,
        String providerId,
        List<PassengerDto> passengers,
        List<FlightOfferDto> offers,
        FlightOfferDto.PriceDto totalPrice,
        String status,
        Instant createdAt,
        Instant updatedAt,
        Instant ticketedAt
) {

    public record PassengerDto(
            String passengerId,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String type
    ) {}
}
