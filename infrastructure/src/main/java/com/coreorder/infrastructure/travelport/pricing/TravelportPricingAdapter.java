package com.coreorder.infrastructure.travelport.pricing;

import com.coreorder.domain.exception.ProviderCommunicationException;
import com.coreorder.domain.model.entity.FlightOffer;
import com.coreorder.domain.model.entity.Itinerary;
import com.coreorder.domain.model.entity.Passenger;
import com.coreorder.domain.model.entity.Segment;
import com.coreorder.domain.model.enums.ProviderType;
import com.coreorder.domain.model.valueobject.AirlineCode;
import com.coreorder.domain.model.valueobject.AirportCode;
import com.coreorder.domain.model.valueobject.BookingClass;
import com.coreorder.domain.model.valueobject.CabinClass;
import com.coreorder.domain.model.valueobject.FlightNumber;
import com.coreorder.domain.model.valueobject.Money;
import com.coreorder.domain.port.out.PricingProvider;
import com.coreorder.infrastructure.travelport.base.TravelportClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Travelport implementation of the PricingProvider port.
 */
@Component
@ConditionalOnProperty(prefix = "providers.travelport", name = "enabled", havingValue = "true")
public class TravelportPricingAdapter implements PricingProvider {

    private final TravelportClient client;

    public TravelportPricingAdapter(TravelportClient client) {
        this.client = client;
    }

    @Override
    public ProviderType providerType() {
        return ProviderType.TRAVELPORT;
    }

    @Override
    public FlightOffer confirmPrice(String offerId, List<Passenger> passengers) {
        try {
            return buildPriceConfirmation(offerId, passengers);
        } catch (RuntimeException e) {
            throw new ProviderCommunicationException(
                    "Failed to confirm price via Travelport: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isEnabled() {
        return client.isEnabled();
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
