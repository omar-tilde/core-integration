package com.coreorder.application.command;

import java.time.LocalDate;
import java.util.List;

import com.coreorder.domain.model.valueobject.PassengerType;

/**
 * Command for creating an order.
 */
public record CreateOrderCommand(
        String offerId,
        String providerId,
        List<PassengerData> passengers,
        PaymentData payment
) {

    public CreateOrderCommand {
        if (offerId == null || offerId.isBlank()) {
            throw new IllegalArgumentException("Offer ID must not be blank");
        }
        if (passengers == null || passengers.isEmpty()) {
            throw new IllegalArgumentException("At least one passenger is required");
        }
        passengers = List.copyOf(passengers);
    }

    public record PassengerData(
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            PassengerType type,
            DocumentData document
    ) {
        public record DocumentData(
                String type,
                String number,
                String issuingCountry,
                LocalDate expirationDate
        ) {}
    }

    public record PaymentData(
            String method,
            String cardNumber,
            String cardHolderName,
            String expiryMonth,
            String expiryYear,
            String cvv
    ) {}
}
