package com.coreorder.infrastructure.provider.travelport;

import java.util.List;

import com.coreorder.domain.model.entity.FlightOffer;
import com.coreorder.domain.model.entity.Itinerary;
import com.coreorder.domain.model.entity.Segment;
import com.coreorder.domain.model.valueobject.AirportCode;
import com.coreorder.domain.model.valueobject.BookingClass;
import com.coreorder.domain.model.valueobject.CabinClass;
import com.coreorder.domain.model.valueobject.FlightNumber;
import com.coreorder.domain.model.valueobject.FlightSearchCriteria;
import com.coreorder.domain.model.valueobject.Money;
import com.coreorder.application.provider.ProviderCommunicationException;
import com.coreorder.domain.port.out.FlightSearchProvider;

/**
 * Travelport implementation of the FlightSearchProvider port.
 * <p>
 * In production, this would call the Travelport Universal API (SOAP/REST).
 * This implementation provides realistic mock data for development and testing.
 */
public class TravelportFlightSearchAdapter implements FlightSearchProvider {

    public static final String PROVIDER_ID = "TRAVELPORT";

    private final TravelportClient client;

    public TravelportFlightSearchAdapter(TravelportClient client) {
        this.client = client;
    }

    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    @Override
    public List<FlightOffer> searchFlights(FlightSearchCriteria criteria) {
        try {
            // Delegate to the Travelport client
            var response = client.searchFlights(criteria);
            return response.stream()
                    .map(this::mapToDomainOffer)
                    .toList();
        } catch (RuntimeException e) {
            throw new ProviderCommunicationException(
                    "Failed to search flights via Travelport: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        return client.isReachable();
    }

    private FlightOffer mapToDomainOffer(TravelportClient.TravelportFlightResponse response) {
        var itineraries = response.itineraries().stream()
                .map(this::mapToItinerary)
                .toList();

        return new FlightOffer(
                "TP-" + response.offerId(),
                itineraries,
                Money.of(response.totalPrice(), response.currency()),
                Money.of(response.basePrice(), response.currency()),
                Money.of(response.taxAmount(), response.currency()),
                response.validatingCarrier(),
                response.refundable(),
                response.seatsAvailable()
        );
    }

    private Itinerary mapToItinerary(TravelportClient.TravelportItineraryResponse tpItinerary) {
        var segments = tpItinerary.segments().stream()
                .map(this::mapToSegment)
                .toList();
        return new Itinerary(segments);
    }

    private Segment mapToSegment(TravelportClient.TravelportSegmentResponse tpSegment) {
        return new Segment(
                FlightNumber.parse(tpSegment.marketingCarrier() + tpSegment.flightNumber()),
                AirportCode.of(tpSegment.origin()),
                AirportCode.of(tpSegment.destination()),
                tpSegment.departureTime(),
                tpSegment.arrivalTime(),
                CabinClass.fromString(tpSegment.cabinClass()),
                BookingClass.of(tpSegment.bookingClass()),
                tpSegment.availableSeats(),
                Segment.AircraftInfo.of(tpSegment.equipmentCode(), tpSegment.aircraftType())
        );
    }
}
