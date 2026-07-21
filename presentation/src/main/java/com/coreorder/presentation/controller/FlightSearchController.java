package com.coreorder.presentation.controller;

import com.coreorder.application.flightsearch.FlightOfferDto;
import com.coreorder.application.flightsearch.FlightSearchService;
import com.coreorder.application.flightsearch.SearchFlightsCommand;
import com.coreorder.presentation.request.FlightSearchRequest;
import com.coreorder.presentation.response.FlightSearchResponse;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for flight search operations.
 * Exposes API endpoints using application commands and services.
 */
@RestController
@RequestMapping("/api/v1/flights")
public class FlightSearchController {

    private final FlightSearchService flightSearchService;

    public FlightSearchController(FlightSearchService flightSearchService) {
        this.flightSearchService = flightSearchService;
    }

    /**
     * Search for available flights.
     */
    @PostMapping("/search")
    public ResponseEntity<FlightSearchResponse> searchFlights(
            @Valid @RequestBody FlightSearchRequest request
    ) {
        SearchFlightsCommand command = mapToCommand(request);
        List<FlightOfferDto> offers = flightSearchService.searchFlights(command);

        var response = new FlightSearchResponse(
                offers,
                offers.size(),
                offers.isEmpty() ? null : offers.getFirst().providerId(),
                flightSearchService.getAvailableProviders()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get the list of available flight search providers.
     */
    @GetMapping("/providers")
    public ResponseEntity<List<String>> getAvailableProviders() {
        return ResponseEntity.ok(flightSearchService.getAvailableProviders());
    }

    private SearchFlightsCommand mapToCommand(FlightSearchRequest request) {
        return new SearchFlightsCommand(
                request.tripType(),
                request.origin(),
                request.destination(),
                LocalDate.parse(request.departureDate()),
                request.returnDate() != null ? LocalDate.parse(request.returnDate()) : null,
                request.adultCount(),
                request.childCount(),
                request.infantCount(),
                request.cabinClass(),
                request.maxResults(),
                request.preferredAirlines(),
                request.preferredProvider()
        );
    }
}
