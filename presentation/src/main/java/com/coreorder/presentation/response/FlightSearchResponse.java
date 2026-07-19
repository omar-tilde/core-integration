package com.coreorder.presentation.response;

import java.util.List;

import com.coreorder.application.dto.FlightOfferDto;

/**
 * REST response model for flight search results.
 */
public record FlightSearchResponse(
        List<FlightOfferDto> offers,
        int totalResults,
        String providerId,
        List<String> availableProviders
) {}
