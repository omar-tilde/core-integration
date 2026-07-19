package com.coreorder.application.service;

import java.util.List;

import com.coreorder.application.command.SearchFlightsCommand;
import com.coreorder.application.dto.FlightOfferDto;
import com.coreorder.application.mapper.FlightOfferMapper;
import com.coreorder.application.provider.ProviderRouter;
import com.coreorder.domain.model.entity.FlightOffer;
import com.coreorder.domain.model.valueobject.FlightSearchCriteria;
import com.coreorder.domain.port.out.FlightSearchProvider;

/**
 * Application service for flight search operations.
 * Orchestrates the search flow without knowing which provider is being used.
 */
public class FlightSearchService {

    private final ProviderRouter providerRouter;

    public FlightSearchService(ProviderRouter providerRouter) {
        this.providerRouter = providerRouter;
    }

    /**
     * Search for flights based on the given command.
     * Routes to the appropriate provider based on the command's preferred provider,
     * or selects the first available provider if none is specified.
     *
     * @param command the search command
     * @return list of flight offer DTOs
     */
    public List<FlightOfferDto> searchFlights(SearchFlightsCommand command) {
        // 1. Resolve provider
        FlightSearchProvider provider = providerRouter.resolveFlightSearchProvider(command.preferredProvider());

        // 2. Map command to domain search criteria
        FlightSearchCriteria criteria = toSearchCriteria(command);

        // 3. Execute search through the provider
        List<FlightOffer> offers = provider.searchFlights(criteria);

        // 4. Map domain results to DTOs
        return FlightOfferMapper.toDtoList(offers, provider.providerId());
    }

    /**
     * Get the list of available flight search provider IDs.
     */
    public List<String> getAvailableProviders() {
        return providerRouter.getAvailableFlightSearchProviderIds();
    }

    private FlightSearchCriteria toSearchCriteria(SearchFlightsCommand command) {
        return new FlightSearchCriteria(
                command.tripType(),
                command.origin(),
                command.destination(),
                command.departureDate(),
                command.returnDate(),
                command.adultCount(),
                command.childCount(),
                command.infantCount(),
                command.cabinClass(),
                command.maxResults(),
                command.preferredAirlines()
        );
    }
}
