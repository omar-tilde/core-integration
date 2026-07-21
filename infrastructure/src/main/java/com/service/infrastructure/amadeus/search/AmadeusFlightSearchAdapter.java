package com.core.service.infrastructure.amadeus.search;

import com.core.service.domain.exception.ProviderCommunicationException;
import com.core.service.domain.model.entity.FlightOffer;
import com.core.service.domain.model.entity.Itinerary;
import com.core.service.domain.model.entity.Segment;
import com.core.service.domain.model.enums.ProviderType;
import com.core.service.domain.model.valueobject.AirportCode;
import com.core.service.domain.model.valueobject.BookingClass;
import com.core.service.domain.model.valueobject.CabinClass;
import com.core.service.domain.model.valueobject.FlightNumber;
import com.core.service.domain.model.valueobject.FlightSearchCriteria;
import com.core.service.domain.model.valueobject.Money;
import com.core.service.domain.port.out.FlightSearchProvider;
import com.core.service.infrastructure.amadeus.base.AmadeusClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Amadeus implementation of the FlightSearchProvider port.
 */
@Component
@ConditionalOnProperty(prefix = "providers.amadeus", name = "enabled", havingValue = "true")
public class AmadeusFlightSearchAdapter implements FlightSearchProvider {

    private final AmadeusClient client;

    public AmadeusFlightSearchAdapter(AmadeusClient client) {
        this.client = client;
    }

    @Override
    public ProviderType providerType() {
        return ProviderType.AMADEUS;
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
    public boolean isEnabled() {
        return client.isEnabled();
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
