package com.coreorder.domain.port.out;

import com.coreorder.domain.model.entity.FlightOffer;
import com.coreorder.domain.model.valueobject.FlightSearchCriteria;

import java.util.List;

/**
 * Outbound port for flight search providers (Travelport, Amadeus, Sabre, etc.).
 */
public interface FlightSearchProvider extends ProviderStrategy {

    /**
     * Search for flights matching the criteria.
     *
     * @param criteria provider-agnostic search input
     * @return list of {@link FlightOffer} objects
     */
    List<FlightOffer> searchFlights(FlightSearchCriteria criteria);
}
