package com.coreorder.domain.port.out;

import java.util.List;

import com.coreorder.domain.model.entity.FlightOffer;
import com.coreorder.domain.model.valueobject.FlightSearchCriteria;

/**
 * Outbound port for flight search providers (Travelport, Amadeus, Sabre, etc.).
 * <p>
 * The domain defines the contract; the infrastructure module provides the implementations.
 * A new provider is added by simply implementing this interface and registering it as a Spring bean.
 */
public interface FlightSearchProvider {

    /**
     * @return stable identifier for this provider, e.g. "AMADEUS" or "TRAVELPORT".
     */
    String providerId();

    /**
     * Search for flights matching the criteria.
     *
     * @param criteria provider-agnostic search input
     * @return list of {@link FlightOffer} objects
     * @throws RuntimeException if the provider cannot complete the request
     */
    List<FlightOffer> searchFlights(FlightSearchCriteria criteria);

    /**
     * @return whether this provider is currently reachable and enabled.
     */
    boolean isAvailable();
}
