package com.coreorder.infrastructure.travelport;

import com.coreorder.domain.model.enums.ProviderType;
import com.coreorder.domain.model.valueobject.AirportCode;
import com.coreorder.domain.model.valueobject.CabinClass;
import com.coreorder.domain.model.valueobject.FlightSearchCriteria;
import com.coreorder.domain.model.valueobject.TripType;
import com.coreorder.infrastructure.travelport.base.TravelportClient;
import com.coreorder.infrastructure.travelport.base.TravelportProperties;
import com.coreorder.infrastructure.travelport.search.TravelportFlightSearchAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TravelportFlightSearchAdapterTest {

    private TravelportFlightSearchAdapter adapter;
    private TravelportClient client;

    @BeforeEach
    void setUp() {
        var properties = new TravelportProperties(
                true, "https://api.travelport.com", "P123",
                "user", "pass", 5000, 30000
        );
        client = new TravelportClient(properties);
        adapter = new TravelportFlightSearchAdapter(client);
    }

    @Test
    void shouldReturnTravelportAsProviderTypeAndId() {
        assertThat(adapter.providerType()).isEqualTo(ProviderType.TRAVELPORT);
        assertThat(adapter.providerId()).isEqualTo("TRAVELPORT");
    }

    @Test
    void shouldBeAvailableWhenEnabled() {
        assertThat(adapter.isEnabled()).isTrue();
    }

    @Test
    void shouldSearchFlightsAndReturnOffers() {
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

        assertThat(offers).isNotEmpty();
        assertThat(offers).hasSize(3);

        var firstOffer = offers.getFirst();
        assertThat(firstOffer.offerId()).startsWith("TP-");
        assertThat(firstOffer.itineraries()).isNotEmpty();
        assertThat(firstOffer.totalPrice().currency().getCurrencyCode()).isEqualTo("EUR");
    }

    @Test
    void shouldReturnOffersWithCorrectSegments() {
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

        var directOffer = offers.getFirst();
        assertThat(directOffer.itineraries()).hasSize(1);
        var itinerary = directOffer.itineraries().getFirst();
        assertThat(itinerary.isDirect()).isTrue();
        assertThat(itinerary.origin().code()).isEqualTo("CDG");
        assertThat(itinerary.destination().code()).isEqualTo("JFK");

        var connectingOffer = offers.get(1);
        assertThat(connectingOffer.itineraries().getFirst().isConnecting()).isTrue();
        assertThat(connectingOffer.itineraries().getFirst().numberOfStops()).isEqualTo(1);
    }

    @Test
    void shouldNotBeAvailableWhenDisabled() {
        var disabledProperties = new TravelportProperties(
                false, "https://api.travelport.com", "P123",
                "user", "pass", 5000, 30000
        );
        var disabledClient = new TravelportClient(disabledProperties);
        var disabledAdapter = new TravelportFlightSearchAdapter(disabledClient);

        assertThat(disabledAdapter.isEnabled()).isFalse();
    }
}
