package com.coreorder.presentation.response;

import com.coreorder.application.flightsearch.FlightOfferDto;

import java.util.List;

/**
 * REST response payload for flight search operations.
 */
public record FlightSearchResponse(
        List<FlightOfferDto> offers,
        int totalResults,
        String selectedProvider,
        List<String> availableProviders
) {}
