package com.coreorder.infrastructure.travelport.search;

import com.coreorder.domain.exception.ProviderCommunicationException;
import com.coreorder.domain.model.entity.FlightOffer;
import com.coreorder.domain.model.entity.Itinerary;
import com.coreorder.domain.model.entity.Segment;
import com.coreorder.domain.model.enums.ProviderType;
import com.coreorder.domain.model.valueobject.AirportCode;
import com.coreorder.domain.model.valueobject.BookingClass;
import com.coreorder.domain.model.valueobject.CabinClass;
import com.coreorder.domain.model.valueobject.FlightNumber;
import com.coreorder.domain.model.valueobject.FlightSearchCriteria;
import com.coreorder.domain.model.valueobject.Money;
import com.coreorder.domain.port.out.FlightSearchProvider;
import com.coreorder.infrastructure.travelport.base.TravelportClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Travelport implementation of the FlightSearchProvider port.
 */
@Component
@ConditionalOnProperty(prefix = "providers.travelport", name = "enabled", havingValue = "true")
public class TravelportFlightSearchAdapter implements FlightSearchProvider {

    private final TravelportClient client;

    public TravelportFlightSearchAdapter(TravelportClient client) {
        this.client = client;
    }

    @Override
    public ProviderType providerType() {
        return ProviderType.TRAVELPORT;
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
                    "Failed to search flights via Travelport: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isEnabled() {
        return client.isEnabled();
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
