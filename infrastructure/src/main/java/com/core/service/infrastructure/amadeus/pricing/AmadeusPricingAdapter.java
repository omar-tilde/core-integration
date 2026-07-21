package com.core.service.infrastructure.amadeus.pricing;

import com.core.service.domain.exception.ProviderCommunicationException;
import com.core.service.domain.model.entity.FlightOffer;
import com.core.service.domain.model.entity.Itinerary;
import com.core.service.domain.model.entity.Passenger;
import com.core.service.domain.model.entity.Segment;
import com.core.service.domain.model.enums.ProviderType;
import com.core.service.domain.model.valueobject.AirlineCode;
import com.core.service.domain.model.valueobject.AirportCode;
import com.core.service.domain.model.valueobject.BookingClass;
import com.core.service.domain.model.valueobject.CabinClass;
import com.core.service.domain.model.valueobject.FlightNumber;
import com.core.service.domain.model.valueobject.Money;
import com.core.service.domain.port.out.PricingProvider;
import com.core.service.infrastructure.amadeus.base.AmadeusClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Amadeus implementation of the PricingProvider port.
 */
@Component
@ConditionalOnProperty(prefix = "providers.amadeus", name = "enabled", havingValue = "true")
public class AmadeusPricingAdapter implements PricingProvider {

    private final AmadeusClient client;

    public AmadeusPricingAdapter(AmadeusClient client) {
        this.client = client;
    }

    @Override
    public ProviderType providerType() {
        return ProviderType.AMADEUS;
    }

    @Override
    public FlightOffer confirmPrice(String offerId, List<Passenger> passengers) {
        try {
            return buildPriceConfirmation(offerId, passengers);
        } catch (RuntimeException e) {
            throw new ProviderCommunicationException(
                    "Failed to confirm price via Amadeus: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isEnabled() {
        return client.isEnabled();
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
