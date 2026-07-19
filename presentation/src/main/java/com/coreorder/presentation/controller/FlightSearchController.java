package com.coreorder.presentation.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coreorder.application.command.SearchFlightsCommand;
import com.coreorder.application.dto.FlightOfferDto;
import com.coreorder.application.service.FlightSearchService;
import com.coreorder.domain.model.valueobject.AirportCode;
import com.coreorder.domain.model.valueobject.CabinClass;
import com.coreorder.domain.model.valueobject.TripType;
import com.coreorder.presentation.request.FlightSearchRequest;
import com.coreorder.presentation.response.FlightSearchResponse;

import jakarta.validation.Valid;

/**
 * REST controller for flight search operations.
 * Provider-agnostic — consumers never know which provider fulfills the request.
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
     * <p>
     * POST /api/v1/flights/search
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
     * <p>
     * GET /api/v1/flights/providers
     */
    @GetMapping("/providers")
    public ResponseEntity<List<String>> getAvailableProviders() {
        return ResponseEntity.ok(flightSearchService.getAvailableProviders());
    }

    private SearchFlightsCommand mapToCommand(FlightSearchRequest request) {
        return new SearchFlightsCommand(
                TripType.valueOf(request.tripType().toUpperCase()),
                AirportCode.of(request.origin().toUpperCase()),
                AirportCode.of(request.destination().toUpperCase()),
                LocalDate.parse(request.departureDate()),
                request.returnDate() != null ? LocalDate.parse(request.returnDate()) : null,
                request.adultCount(),
                request.childCount(),
                request.infantCount(),
                CabinClass.fromString(request.cabinClass()),
                request.maxResults(),
                request.preferredAirlines(),
                request.preferredProvider()
        );
    }
}
