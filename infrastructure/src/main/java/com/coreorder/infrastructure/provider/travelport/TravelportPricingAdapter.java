package com.coreorder.infrastructure.provider.travelport;

import java.time.LocalDateTime;
import java.util.List;

import com.coreorder.domain.model.entity.FlightOffer;
import com.coreorder.domain.model.entity.Itinerary;
import com.coreorder.domain.model.entity.Passenger;
import com.coreorder.domain.model.entity.Segment;
import com.coreorder.domain.model.valueobject.AirlineCode;
import com.coreorder.domain.model.valueobject.AirportCode;
import com.coreorder.domain.model.valueobject.BookingClass;
import com.coreorder.domain.model.valueobject.CabinClass;
import com.coreorder.domain.model.valueobject.FlightNumber;
import com.coreorder.domain.model.valueobject.Money;
import com.coreorder.application.provider.ProviderCommunicationException;
import com.coreorder.domain.port.out.PricingProvider;

/**
 * Travelport implementation of the PricingProvider port.
 * Handles price confirmation/fare revalidation through Travelport's pricing API.
 */
public class TravelportPricingAdapter implements PricingProvider {

    public static final String PROVIDER_ID = "TRAVELPORT";

    private final TravelportClient client;

    public TravelportPricingAdapter(TravelportClient client) {
        this.client = client;
    }

    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    @Override
    public FlightOffer confirmPrice(String offerId, List<Passenger> passengers) {
        try {
            // In production: call Travelport's Air Price API
            // For now: return the offer with confirmed pricing
            return buildPriceConfirmation(offerId, passengers);
        } catch (RuntimeException e) {
            throw new ProviderCommunicationException(
                    "Failed to confirm price via Travelport: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        return client.isReachable();
    }

    private FlightOffer buildPriceConfirmation(String offerId, List<Passenger> passengers) {
        int passengerCount = passengers.size();
        double basePerPassenger = 350.00;
        double taxPerPassenger = 100.00;
        double totalBase = basePerPassenger * passengerCount;
        double totalTax = taxPerPassenger * passengerCount;

        var segment = new Segment(
                FlightNumber.of(AirlineCode.of("AF"), "1234"),
                AirportCode.of("CDG"), AirportCode.of("JFK"),
                LocalDateTime.of(2026, 8, 15, 10, 0),
                LocalDateTime.of(2026, 8, 15, 13, 30),
                CabinClass.ECONOMY, BookingClass.of("Y"),
                Math.max(0, 15 - passengerCount),
                Segment.AircraftInfo.of("77W", "Boeing 777-300ER")
        );

        return new FlightOffer(
                offerId,
                List.of(new Itinerary(List.of(segment))),
                Money.of(totalBase + totalTax, "EUR"),
                Money.of(totalBase, "EUR"),
                Money.of(totalTax, "EUR"),
                "AF", true,
                Math.max(0, 15 - passengerCount)
        );
    }
}
