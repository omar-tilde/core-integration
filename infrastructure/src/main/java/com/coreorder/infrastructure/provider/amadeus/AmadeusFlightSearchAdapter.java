package com.coreorder.infrastructure.provider.amadeus;

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
 * Amadeus implementation of the FlightSearchProvider port.
 * <p>
 * In production, this would call Amadeus Flight Offers Search API.
 */
public class AmadeusFlightSearchAdapter implements FlightSearchProvider {

    public static final String PROVIDER_ID = "AMADEUS";

    private final AmadeusClient client;

    public AmadeusFlightSearchAdapter(AmadeusClient client) {
        this.client = client;
    }

    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    @Override
    public List<FlightOffer> searchFlights(FlightSearchCriteria criteria) {
        try {
            var response = client.searchFlights(criteria);
            return response.stream()
                    .map(this::mapToDomainOffer)
                    .toList();
        } catch (RuntimeException e) {
            throw new ProviderCommunicationException(
                    "Failed to search flights via Amadeus: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        return client.isReachable();
    }

    private FlightOffer mapToDomainOffer(AmadeusClient.AmadeusFlightOfferResponse response) {
        var itineraries = response.itineraries().stream()
                .map(this::mapToItinerary)
                .toList();

        return new FlightOffer(
                "AMA-" + response.offerId(),
                itineraries,
                Money.of(response.totalPrice(), response.currency()),
                Money.of(response.basePrice(), response.currency()),
                Money.of(response.taxAmount(), response.currency()),
                response.validatingCarrier(),
                response.refundable(),
                response.seatsAvailable()
        );
    }

    private Itinerary mapToItinerary(AmadeusClient.AmadeusItineraryResponse amaItinerary) {
        var segments = amaItinerary.segments().stream()
                .map(this::mapToSegment)
                .toList();
        return new Itinerary(segments);
    }

    private Segment mapToSegment(AmadeusClient.AmadeusSegmentResponse amaSegment) {
        return new Segment(
                FlightNumber.parse(amaSegment.marketingCarrier() + amaSegment.flightNumber()),
                AirportCode.of(amaSegment.origin()),
                AirportCode.of(amaSegment.destination()),
                amaSegment.departureTime(),
                amaSegment.arrivalTime(),
                CabinClass.fromString(amaSegment.cabinClass()),
                BookingClass.of(amaSegment.bookingClass()),
                amaSegment.availableSeats(),
                Segment.AircraftInfo.of(amaSegment.equipmentCode(), amaSegment.aircraftType())
        );
    }
}
