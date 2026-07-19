package com.coreorder.domain.model.entity;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import com.coreorder.domain.model.valueobject.Money;

/**
 * A flight offer represents a priced combination of itineraries that a provider is willing to sell.
 * For a round-trip, there are two itineraries; for one-way, one.
 * Immutable domain entity.
 */
public record FlightOffer(
        String offerId,
        List<Itinerary> itineraries,
        Money totalPrice,
        Money basePrice,
        Money taxAmount,
        String validatingCarrier,
        boolean refundable,
        int seatsAvailable
) {

    public FlightOffer {
        Objects.requireNonNull(offerId, "Offer ID must not be null");
        Objects.requireNonNull(itineraries, "Itineraries must not be null");
        Objects.requireNonNull(totalPrice, "Total price must not be null");
        Objects.requireNonNull(basePrice, "Base price must not be null");
        Objects.requireNonNull(taxAmount, "Tax amount must not be null");

        if (itineraries.isEmpty() || itineraries.size() > 3) {
            throw new IllegalArgumentException("Must have between 1 and 3 itineraries");
        }
        itineraries = List.copyOf(itineraries);
    }

    /**
     * Total travel duration across all itineraries.
     */
    public Duration totalTravelDuration() {
        return itineraries.stream()
                .map(Itinerary::totalDuration)
                .reduce(Duration.ZERO, Duration::plus);
    }

    /**
     * Whether this is a one-way offer.
     */
    public boolean isOneWay() {
        return itineraries.size() == 1;
    }

    /**
     * Whether this is a round-trip offer.
     */
    public boolean isRoundTrip() {
        return itineraries.size() == 2;
    }
}
