package com.coreorder.presentation.controller;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.coreorder.application.command.SearchFlightsCommand;
import com.coreorder.application.dto.FlightOfferDto;
import com.coreorder.application.service.FlightSearchService;
import com.coreorder.presentation.exception.GlobalExceptionHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@org.springframework.context.annotation.Import({GlobalExceptionHandler.class, FlightSearchController.class})
class FlightSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FlightSearchService flightSearchService;

    @Test
    void shouldSearchFlightsSuccessfully() throws Exception {
        var offers = List.of(createSampleOffer());
        when(flightSearchService.searchFlights(any(SearchFlightsCommand.class))).thenReturn(offers);
        when(flightSearchService.getAvailableProviders()).thenReturn(List.of("TRAVELPORT", "AMADEUS"));

        var request = """
                {
                    "tripType": "ONE_WAY",
                    "origin": "CDG",
                    "destination": "JFK",
                    "departureDate": "2026-08-15",
                    "adultCount": 1,
                    "childCount": 0,
                    "infantCount": 0,
                    "cabinClass": "ECONOMY",
                    "maxResults": 50
                }
                """;

        mockMvc.perform(post("/api/v1/flights/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offers").isArray())
                .andExpect(jsonPath("$.offers[0].offerId").value("OFFER-1"))
                .andExpect(jsonPath("$.offers[0].providerId").value("TRAVELPORT"))
                .andExpect(jsonPath("$.totalResults").value(1))
                .andExpect(jsonPath("$.availableProviders").isArray());
    }

    @Test
    void shouldReturnBadRequestWhenOriginMissing() throws Exception {
        var request = """
                {
                    "tripType": "ONE_WAY",
                    "destination": "JFK",
                    "departureDate": "2026-08-15",
                    "adultCount": 1,
                    "cabinClass": "ECONOMY",
                    "maxResults": 50
                }
                """;

        mockMvc.perform(post("/api/v1/flights/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"));
    }

    @Test
    void shouldReturnBadRequestWhenInvalidOrigin() throws Exception {
        var request = """
                {
                    "tripType": "ONE_WAY",
                    "origin": "CD",
                    "destination": "JFK",
                    "departureDate": "2026-08-15",
                    "adultCount": 1,
                    "cabinClass": "ECONOMY",
                    "maxResults": 50
                }
                """;

        mockMvc.perform(post("/api/v1/flights/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetAvailableProviders() throws Exception {
        when(flightSearchService.getAvailableProviders()).thenReturn(List.of("TRAVELPORT", "AMADEUS"));

        mockMvc.perform(get("/api/v1/flights/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("TRAVELPORT"))
                .andExpect(jsonPath("$[1]").value("AMADEUS"));
    }

    @Test
    void shouldSearchFlightsWithPreferredProvider() throws Exception {
        var offers = List.of(createSampleOffer());
        when(flightSearchService.searchFlights(any(SearchFlightsCommand.class))).thenReturn(offers);
        when(flightSearchService.getAvailableProviders()).thenReturn(List.of("TRAVELPORT", "AMADEUS"));

        var request = """
                {
                    "tripType": "ONE_WAY",
                    "origin": "CDG",
                    "destination": "JFK",
                    "departureDate": "2026-08-15",
                    "adultCount": 1,
                    "cabinClass": "ECONOMY",
                    "maxResults": 50,
                    "preferredProvider": "AMADEUS"
                }
                """;

        mockMvc.perform(post("/api/v1/flights/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offers[0].providerId").value("TRAVELPORT"));
    }

    private FlightOfferDto createSampleOffer() {
        return new FlightOfferDto(
                "OFFER-1",
                "TRAVELPORT",
                List.of(new FlightOfferDto.ItineraryDto(
                        "CDG", "JFK",
                        LocalDateTime.of(2026, 8, 15, 10, 0),
                        LocalDateTime.of(2026, 8, 15, 13, 30),
                        Duration.ofHours(3).plusMinutes(30),
                        0,
                        List.of(new FlightOfferDto.SegmentDto(
                                "AF1234", "CDG", "JFK",
                                LocalDateTime.of(2026, 8, 15, 10, 0),
                                LocalDateTime.of(2026, 8, 15, 13, 30),
                                Duration.ofHours(3).plusMinutes(30),
                                "ECONOMY", "Y", 15, "77W", "Boeing 777-300ER"
                        ))
                )),
                new FlightOfferDto.PriceDto(BigDecimal.valueOf(450.00), "EUR"),
                new FlightOfferDto.PriceDto(BigDecimal.valueOf(350.00), "EUR"),
                new FlightOfferDto.PriceDto(BigDecimal.valueOf(100.00), "EUR"),
                "AF", true, 15
        );
    }
}
