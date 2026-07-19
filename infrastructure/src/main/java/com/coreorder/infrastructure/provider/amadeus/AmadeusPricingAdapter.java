package com.coreorder.infrastructure.provider.amadeus;

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
 * Amadeus implementation of the PricingProvider port.
 * Handles price confirmation through Amadeus Flight Offers Price API.
 */
public class AmadeusPricingAdapter implements PricingProvider {

    public static final String PROVIDER_ID = "AMADEUS";

    private final AmadeusClient client;

    public AmadeusPricingAdapter(AmadeusClient client) {
        this.client = client;
    }

    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    @Override
    public FlightOffer confirmPrice(String offerId, List<Passenger> passengers) {
        try {
            // In production: call Amadeus Flight Offers Price API
            return buildPriceConfirmation(offerId, passengers);
        } catch (RuntimeException e) {
            throw new ProviderCommunicationException(
                    "Failed to confirm price via Amadeus: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        return client.isReachable();
    }

    private FlightOffer buildPriceConfirmation(String offerId, List<Passenger> passengers) {
        int passengerCount = passengers.size();
        double basePerPassenger = 370.00;
        double taxPerPassenger = 105.00;
        double totalBase = basePerPassenger * passengerCount;
        double totalTax = taxPerPassenger * passengerCount;

        var segment = new Segment(
                FlightNumber.of(AirlineCode.of("AF"), "6001"),
                AirportCode.of("CDG"), AirportCode.of("JFK"),
                LocalDateTime.of(2026, 8, 15, 9, 0),
                LocalDateTime.of(2026, 8, 15, 12, 45),
                CabinClass.ECONOMY, BookingClass.of("Y"),
                Math.max(0, 12 - passengerCount),
                Segment.AircraftInfo.of("359", "Airbus A350-900")
        );

        return new FlightOffer(
                offerId,
                List.of(new Itinerary(List.of(segment))),
                Money.of(totalBase + totalTax, "EUR"),
                Money.of(totalBase, "EUR"),
                Money.of(totalTax, "EUR"),
                "AF", true,
                Math.max(0, 12 - passengerCount)
        );
    }
}
