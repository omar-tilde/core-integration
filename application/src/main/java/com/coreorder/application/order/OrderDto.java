package com.coreorder.application.order;

import com.coreorder.application.flightsearch.FlightOfferDto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object for orders.
 */
public record OrderDto(
        String orderId,
        String pnr,
        String providerId,
        String status,
        FlightOfferDto.PriceDto totalPrice,
        List<PassengerDto> passengers,
        List<FlightOfferDto> offers,
        Instant createdAt
) {
    public record PassengerDto(
            String id,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String type,
            DocumentDto document
    ) {}

    public record DocumentDto(
            String type,
            String number,
            String issuingCountry,
            LocalDate expirationDate
    ) {}
}
