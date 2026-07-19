package com.coreorder.domain.port.out;

import java.util.List;

import com.coreorder.domain.model.entity.FlightOffer;
import com.coreorder.domain.model.entity.Passenger;

/**
 * Outbound port for pricing providers (Travelport, Amadeus, Sabre, etc.).
 * <p>
 * Re-prices an existing offer to capture fare fluctuations, fare-class availability and
 * per-passenger totals before the order is created.
 */
public interface PricingProvider {

    /**
     * @return stable identifier for this provider, e.g. "AMADEUS" or "TRAVELPORT".
     */
    String providerId();

    /**
     * Re-price an offer for a specific set of passengers.
     *
     * @param offerId    identifier of the offer to re-price
     * @param passengers passengers that will travel on the resulting order
     * @return {@link FlightOffer} reflecting the freshly confirmed price
     */
    FlightOffer confirmPrice(String offerId, List<Passenger> passengers);

    /**
     * @return whether this provider is currently reachable and enabled.
     */
    boolean isAvailable();
}
