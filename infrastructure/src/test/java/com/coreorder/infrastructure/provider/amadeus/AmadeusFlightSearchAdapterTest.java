package com.coreorder.infrastructure.provider.amadeus;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.coreorder.domain.model.valueobject.AirportCode;
import com.coreorder.domain.model.valueobject.CabinClass;
import com.coreorder.domain.model.valueobject.FlightSearchCriteria;
import com.coreorder.domain.model.valueobject.TripType;

import static org.assertj.core.api.Assertions.assertThat;

class AmadeusFlightSearchAdapterTest {

    private AmadeusFlightSearchAdapter adapter;
    private AmadeusClient client;

    @BeforeEach
    void setUp() {
        var properties = new AmadeusProperties(
                true, "https://api.amadeus.com/v2",
                "client-id", "client-secret", 5000, 30000
        );
        client = new AmadeusClient(properties);
        adapter = new AmadeusFlightSearchAdapter(client);
    }

    @Test
    void shouldReturnAmadeusAsProviderId() {
        assertThat(adapter.providerId()).isEqualTo("AMADEUS");
    }

    @Test
    void shouldBeAvailableWhenEnabled() {
        assertThat(adapter.isAvailable()).isTrue();
    }

    @Test
    void shouldSearchFlightsAndReturnOffers() {
        var criteria = new FlightSearchCriteria(
                TripType.ONE_WAY,
                AirportCode.of("CDG"),
                AirportCode.of("JFK"),
                LocalDate.of(2026, 8, 15),
                null,
                2, 1, 0,
                CabinClass.ECONOMY,
                100,
                List.of("AF", "LH")
        );

        var offers = adapter.searchFlights(criteria);

        assertThat(offers).isNotEmpty();
        assertThat(offers).hasSize(3); // mock returns 3 offers

        var firstOffer = offers.getFirst();
        assertThat(firstOffer.offerId()).startsWith("AMA-");
        assertThat(firstOffer.totalPrice().currency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(firstOffer.refundable()).isTrue();
    }

    @Test
    void shouldReturnOfferWithCorrectFlightDetails() {
        var criteria = new FlightSearchCriteria(
                TripType.ONE_WAY,
                AirportCode.of("CDG"),
                AirportCode.of("JFK"),
                LocalDate.of(2026, 8, 15),
                null,
                1, 0, 0,
                CabinClass.ECONOMY,
                50,
                List.of()
        );

        var offers = adapter.searchFlights(criteria);

        // Third offer is a connecting flight via FRA
        var connectingOffer = offers.get(2);
        var itinerary = connectingOffer.itineraries().getFirst();
        assertThat(itinerary.isConnecting()).isTrue();
        assertThat(itinerary.numberOfStops()).isEqualTo(1);

        var firstSegment = itinerary.segments().getFirst();
        assertThat(firstSegment.origin().code()).isEqualTo("CDG");
        assertThat(firstSegment.destination().code()).isEqualTo("FRA");

        var secondSegment = itinerary.segments().get(1);
        assertThat(secondSegment.origin().code()).isEqualTo("FRA");
        assertThat(secondSegment.destination().code()).isEqualTo("JFK");
    }

    @Test
    void shouldNotBeAvailableWhenDisabled() {
        var disabledProperties = new AmadeusProperties(
                false, "https://api.amadeus.com/v2",
                "client-id", "client-secret", 5000, 30000
        );
        var disabledClient = new AmadeusClient(disabledProperties);
        var disabledAdapter = new AmadeusFlightSearchAdapter(disabledClient);

        assertThat(disabledAdapter.isAvailable()).isFalse();
    }
}
