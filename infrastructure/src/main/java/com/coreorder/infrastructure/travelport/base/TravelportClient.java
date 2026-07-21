package com.coreorder.infrastructure.travelport.base;

import com.coreorder.domain.model.valueobject.FlightSearchCriteria;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HTTP client for the Travelport Universal API.
 */
@Component
@ConditionalOnProperty(prefix = "providers.travelport", name = "enabled", havingValue = "true")
public class TravelportClient {

    private final TravelportProperties properties;

    public TravelportClient(TravelportProperties properties) {
        this.properties = properties;
    }

    /**
     * Search for flights via Travelport API.
     */
    public List<TravelportFlightResponse> searchFlights(FlightSearchCriteria criteria) {
        return generateMockResponses(criteria);
    }

    /**
     * Check if the Travelport provider is enabled.
     */
    public boolean isEnabled() {
        return properties.enabled();
    }

    private List<TravelportFlightResponse> generateMockResponses(FlightSearchCriteria criteria) {
        String origin = criteria.origin().code();
        String destination = criteria.destination().code();
        LocalDateTime departure = criteria.departureDate().atTime(10, 0);

        // Direct flight offer
        var directOffer = new TravelportFlightResponse(
                "OFFER-001",
                List.of(new TravelportItineraryResponse(
                        List.of(new TravelportSegmentResponse(
                                "AF", "1234", origin, destination,
                                departure, departure.plusHours(3).plusMinutes(30),
                                criteria.cabinClass().name(), "Y", 15,
                                "77W", "Boeing 777-300ER"
                        ))
                )),
                450.00, 350.00, 100.00, "EUR", "AF", true, 15
        );

        // Connecting flight offer
        var connectingOffer = new TravelportFlightResponse(
                "OFFER-002",
                List.of(new TravelportItineraryResponse(
                        List.of(
                                new TravelportSegmentResponse(
                                        "AF", "100", origin, "LHR",
                                        departure.minusHours(2), departure,
                                        criteria.cabinClass().name(), "Y", 20,
                                        "32A", "Airbus A320"
                                ),
                                new TravelportSegmentResponse(
                                        "BA", "200", "LHR", destination,
                                        departure.plusHours(1), departure.plusHours(4),
                                        criteria.cabinClass().name(), "Y", 8,
                                        "789", "Boeing 787-9"
                                )
                        )
                )),
                380.00, 290.00, 90.00, "EUR", "AF", true, 8
        );

        // Premium cabin offer
        var premiumOffer = new TravelportFlightResponse(
                "OFFER-003",
                List.of(new TravelportItineraryResponse(
                        List.of(new TravelportSegmentResponse(
                                "AF", "1234", origin, destination,
                                departure.plusHours(2), departure.plusHours(5).plusMinutes(30),
                                "BUSINESS", "J", 5,
                                "77W", "Boeing 777-300ER"
                        ))
                )),
                1850.00, 1500.00, 350.00, "EUR", "AF", true, 5
        );

        return List.of(directOffer, connectingOffer, premiumOffer);
    }

    // --- Travelport API Response DTOs ---

    public record TravelportFlightResponse(
            String offerId,
            List<TravelportItineraryResponse> itineraries,
            double totalPrice,
            double basePrice,
            double taxAmount,
            String currency,
            String validatingCarrier,
            boolean refundable,
            int seatsAvailable
    ) {}

    public record TravelportItineraryResponse(List<TravelportSegmentResponse> segments) {}

    public record TravelportSegmentResponse(
            String marketingCarrier,
            String flightNumber,
            String origin,
            String destination,
            LocalDateTime departureTime,
            LocalDateTime arrivalTime,
            String cabinClass,
            String bookingClass,
            int availableSeats,
            String equipmentCode,
            String aircraftType
    ) {}
}
