package com.coreorder.application.flightsearch;

import com.coreorder.application.base.exception.InvalidCommandException;
import com.coreorder.application.base.exception.ProviderCommunicationException;
import com.coreorder.application.base.provider.ProviderRouter;
import com.coreorder.domain.exception.DomainException;
import com.coreorder.domain.model.entity.FlightOffer;
import com.coreorder.domain.model.valueobject.AirportCode;
import com.coreorder.domain.model.valueobject.CabinClass;
import com.coreorder.domain.model.valueobject.FlightSearchCriteria;
import com.coreorder.domain.model.valueobject.TripType;
import com.coreorder.domain.port.out.FlightSearchProvider;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service for flight search operations.
 * Maps application commands to domain search criteria and converts domain exceptions to application exceptions.
 */
@Service
public class FlightSearchService {

    private final ProviderRouter providerRouter;

    public FlightSearchService(ProviderRouter providerRouter) {
        this.providerRouter = providerRouter;
    }

    /**
     * Search for flights based on the given command.
     */
    public List<FlightOfferDto> searchFlights(SearchFlightsCommand command) {
        FlightSearchProvider provider = providerRouter.resolveFlightSearchProvider(command.preferredProvider());
        FlightSearchCriteria criteria = toSearchCriteria(command);

        try {
            List<FlightOffer> offers = provider.searchFlights(criteria);
            return FlightOfferMapper.toDtoList(offers, provider.providerId());
        } catch (DomainException e) {
            throw new ProviderCommunicationException(e.getMessage(), e);
        }
    }

    /**
     * Get the list of available flight search provider IDs.
     */
    public List<String> getAvailableProviders() {
        return providerRouter.getAvailableFlightSearchProviderIds();
    }

    private FlightSearchCriteria toSearchCriteria(SearchFlightsCommand command) {
        try {
            return new FlightSearchCriteria(
                    TripType.valueOf(command.tripType().toUpperCase()),
                    AirportCode.of(command.origin().toUpperCase()),
                    AirportCode.of(command.destination().toUpperCase()),
                    command.departureDate(),
                    command.returnDate(),
                    command.adultCount(),
                    command.childCount(),
                    command.infantCount(),
                    CabinClass.fromString(command.cabinClass()),
                    command.maxResults(),
                    command.preferredAirlines()
            );
        } catch (IllegalArgumentException e) {
            throw new InvalidCommandException("Invalid search criteria parameters: " + e.getMessage());
        }
    }
}
