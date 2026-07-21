package com.core.service.application.order;

import com.core.service.application.base.exception.InvalidCommandException;

import java.time.LocalDate;
import java.util.List;

/**
 * Command input object for creating a new order.
 */
public record CreateOrderCommand(
        String offerId,
        String providerId,
        List<PassengerData> passengers,
        PaymentData payment
) {
    public CreateOrderCommand {
        if (offerId == null || offerId.isBlank()) {
            throw new InvalidCommandException("Offer ID must not be null or blank");
        }
        if (passengers == null || passengers.isEmpty()) {
            throw new InvalidCommandException("At least one passenger is required");
        }
        if (payment == null) {
            throw new InvalidCommandException("Payment information must not be null");
        }
    }

    public record PassengerData(
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String type,
            DocumentData document
    ) {
        public PassengerData {
            if (firstName == null || firstName.isBlank()) {
                throw new InvalidCommandException("Passenger first name must not be blank");
            }
            if (lastName == null || lastName.isBlank()) {
                throw new InvalidCommandException("Passenger last name must not be blank");
            }
            if (dateOfBirth == null) {
                throw new InvalidCommandException("Passenger date of birth must not be null");
            }
            if (type == null || type.isBlank()) {
                type = "ADULT";
            }
        }

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
    ) {
        public PaymentData {
            if (method == null || method.isBlank()) {
                method = "CARD";
            }
            if (cardNumber == null || cardNumber.isBlank()) {
                throw new InvalidCommandException("Card number must not be blank");
            }
            if (cardHolderName == null || cardHolderName.isBlank()) {
                throw new InvalidCommandException("Cardholder name must not be blank");
            }
            if (expiryMonth == null || expiryMonth.isBlank()) {
                throw new InvalidCommandException("Expiry month must not be blank");
            }
            if (expiryYear == null || expiryYear.isBlank()) {
                throw new InvalidCommandException("Expiry year must not be blank");
            }
            if (cvv == null || cvv.isBlank()) {
                throw new InvalidCommandException("CVV must not be blank");
            }
        }
    }
}
