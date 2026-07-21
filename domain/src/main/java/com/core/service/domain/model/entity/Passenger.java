package com.core.service.domain.model.entity;

import com.core.service.domain.model.valueobject.PassengerType;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A passenger in an order. Immutable domain entity.
 */
public record Passenger(
        String passengerId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        PassengerType type,
        DocumentInfo documentInfo
) {

    public Passenger {
        Objects.requireNonNull(passengerId, "Passenger ID must not be null");
        Objects.requireNonNull(firstName, "First name must not be null");
        Objects.requireNonNull(lastName, "Last name must not be null");
        Objects.requireNonNull(dateOfBirth, "Date of birth must not be null");
        Objects.requireNonNull(type, "Passenger type must not be null");

        firstName = firstName.trim();
        lastName = lastName.trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            throw new IllegalArgumentException("First and last name must not be blank");
        }
    }

    public String fullName() {
        return firstName + " " + lastName;
    }

    public boolean isAdult() {
        return type == PassengerType.ADULT;
    }

    /**
     * Travel document information (optional for search, required for booking).
     */
    public record DocumentInfo(
            DocumentType type,
            String number,
            String issuingCountry,
            LocalDate expirationDate
    ) {
        public DocumentInfo {
            Objects.requireNonNull(type, "Document type must not be null");
            Objects.requireNonNull(number, "Document number must not be null");
            Objects.requireNonNull(issuingCountry, "Issuing country must not be null");
            Objects.requireNonNull(expirationDate, "Expiration date must not be null");
            number = number.trim();
            if (number.isEmpty()) {
                throw new IllegalArgumentException("Document number must not be blank");
            }
        }
    }

    public enum DocumentType {
        PASSPORT,
        NATIONAL_ID,
        OTHER
    }
}
