package com.coreorder.domain.port.out;

import com.coreorder.domain.model.entity.FlightOffer;
import com.coreorder.domain.model.entity.Passenger;

import java.util.List;

/**
 * Outbound port for pricing providers (Travelport, Amadeus, Sabre, etc.).
 */
public interface PricingProvider extends ProviderStrategy {

    /**
     * Re-price an offer for a specific set of passengers.
     *
     * @param offerId    identifier of the offer to re-price
     * @param passengers passengers that will travel on the resulting order
     * @return {@link FlightOffer} reflecting the freshly confirmed price
     */
    FlightOffer confirmPrice(String offerId, List<Passenger> passengers);
}
