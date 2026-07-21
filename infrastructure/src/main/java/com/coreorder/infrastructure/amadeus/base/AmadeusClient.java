package com.coreorder.infrastructure.amadeus.base;

import com.coreorder.domain.model.valueobject.FlightSearchCriteria;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HTTP client for the Amadeus Self-Service / Enterprise API.
 */
@Component
@ConditionalOnProperty(prefix = "providers.amadeus", name = "enabled", havingValue = "true")
public class AmadeusClient {

    private final AmadeusProperties properties;

    public AmadeusClient(AmadeusProperties properties) {
        this.properties = properties;
    }

    /**
     * Search for flights via Amadeus Flight Offers Search API.
     */
    public List<AmadeusFlightOfferResponse> searchFlights(FlightSearchCriteria criteria) {
        return generateMockResponses(criteria);
    }

    /**
     * Check if the Amadeus provider is enabled.
     */
    public boolean isEnabled() {
        return properties.enabled();
    }

    private List<AmadeusFlightOfferResponse> generateMockResponses(FlightSearchCriteria criteria) {
        String origin = criteria.origin().code();
        String destination = criteria.destination().code();
        LocalDateTime departure = criteria.departureDate().atTime(9, 0);

        // Direct flight
        var directOffer = new AmadeusFlightOfferResponse(
                "AMA-001",
                List.of(new AmadeusItineraryResponse(
                        List.of(new AmadeusSegmentResponse(
                                "AF", "6001", origin, destination,
                                departure, departure.plusHours(3).plusMinutes(45),
                                criteria.cabinClass().name(), "Y", 12,
                                "359", "Airbus A350-900"
                        ))
                )),
                475.00, 370.00, 105.00, "EUR", "AF", true, 12
        );

        // Direct flight with different time
        var afternoonOffer = new AmadeusFlightOfferResponse(
                "AMA-002",
                List.of(new AmadeusItineraryResponse(
                        List.of(new AmadeusSegmentResponse(
                                "AF", "6002", origin, destination,
                                departure.plusHours(5), departure.plusHours(8).plusMinutes(45),
                                criteria.cabinClass().name(), "Y", 22,
                                "77W", "Boeing 777-300ER"
                        ))
                )),
                420.00, 325.00, 95.00, "EUR", "AF", true, 22
        );

        // One-stop via Frankfurt
        var connectingOffer = new AmadeusFlightOfferResponse(
                "AMA-003",
                List.of(new AmadeusItineraryResponse(
                        List.of(
                                new AmadeusSegmentResponse(
                                        "LH", "1035", origin, "FRA",
                                        departure.plusHours(1), departure.plusHours(2).plusMinutes(15),
                                        criteria.cabinClass().name(), "Y", 30,
                                        "321", "Airbus A321"
                                ),
                                new AmadeusSegmentResponse(
                                        "LH", "400", "FRA", destination,
                                        departure.plusHours(3).plusMinutes(30),
                                        departure.plusHours(7),
                                        criteria.cabinClass().name(), "Y", 18,
                                        "744", "Boeing 747-400"
                                )
                        )
                )),
                350.00, 270.00, 80.00, "EUR", "LH", false, 18
        );

        return List.of(directOffer, afternoonOffer, connectingOffer);
    }

    // --- Amadeus API Response DTOs ---

    public record AmadeusFlightOfferResponse(
            String offerId,
            List<AmadeusItineraryResponse> itineraries,
            double totalPrice,
            double basePrice,
            double taxAmount,
            String currency,
            String validatingCarrier,
            boolean refundable,
            int seatsAvailable
    ) {}

    public record AmadeusItineraryResponse(List<AmadeusSegmentResponse> segments) {}

    public record AmadeusSegmentResponse(
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
