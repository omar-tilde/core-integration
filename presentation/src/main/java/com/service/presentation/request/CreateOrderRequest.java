package com.core.service.presentation.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * REST request model for creating an order.
 */
public record CreateOrderRequest(

        @NotBlank(message = "Offer ID is required")
        String offerId,

        String providerId,

        @NotEmpty(message = "At least one passenger is required")
        @Valid
        List<PassengerRequest> passengers,

        @NotNull(message = "Payment information is required")
        @Valid
        PaymentRequest payment
) {

    public record PassengerRequest(
            @NotBlank(message = "First name is required")
            String firstName,

            @NotBlank(message = "Last name is required")
            String lastName,

            @NotBlank(message = "Date of birth is required")
            String dateOfBirth,

            @NotBlank(message = "Passenger type is required")
            String type,

            DocumentRequest document
    ) {
        public record DocumentRequest(
                @NotBlank(message = "Document type is required")
                String type,

                @NotBlank(message = "Document number is required")
                String number,

                @NotBlank(message = "Issuing country is required")
                String issuingCountry,

                @NotBlank(message = "Expiration date is required")
                String expirationDate
        ) {}
    }

    public record PaymentRequest(
            @NotBlank(message = "Payment method is required")
            String method,

            @NotBlank(message = "Card number is required")
            String cardNumber,

            @NotBlank(message = "Card holder name is required")
            String cardHolderName,

            @NotBlank(message = "Expiry month is required")
            String expiryMonth,

            @NotBlank(message = "Expiry year is required")
            String expiryYear,

            @NotBlank(message = "CVV is required")
            String cvv
    ) {}
}
