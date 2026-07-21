package com.core.service.infrastructure.amadeus;

import com.core.service.domain.model.enums.ProviderType;
import com.core.service.domain.model.valueobject.AirportCode;
import com.core.service.domain.model.valueobject.CabinClass;
import com.core.service.domain.model.valueobject.FlightSearchCriteria;
import com.core.service.domain.model.valueobject.TripType;
import com.core.service.infrastructure.amadeus.base.AmadeusClient;
import com.core.service.infrastructure.amadeus.base.AmadeusProperties;
import com.core.service.infrastructure.amadeus.search.AmadeusFlightSearchAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

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
    void shouldReturnAmadeusAsProviderTypeAndId() {
        assertThat(adapter.providerType()).isEqualTo(ProviderType.AMADEUS);
        assertThat(adapter.providerId()).isEqualTo("AMADEUS");
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
                2, 1, 0,
                CabinClass.ECONOMY,
                100,
                List.of("AF", "LH")
        );

        var offers = adapter.searchFlights(criteria);

        assertThat(offers).isNotEmpty();
        assertThat(offers).hasSize(3);

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

        assertThat(disabledAdapter.isEnabled()).isFalse();
    }
}
