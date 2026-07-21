package com.coreorder.application.flightsearch;

import com.coreorder.application.base.exception.ProviderNotFoundException;
import com.coreorder.application.base.provider.ProviderRouter;
import com.coreorder.domain.model.entity.FlightOffer;
import com.coreorder.domain.model.entity.Itinerary;
import com.coreorder.domain.model.entity.Segment;
import com.coreorder.domain.model.enums.ProviderType;
import com.coreorder.domain.model.valueobject.AirlineCode;
import com.coreorder.domain.model.valueobject.AirportCode;
import com.coreorder.domain.model.valueobject.BookingClass;
import com.coreorder.domain.model.valueobject.CabinClass;
import com.coreorder.domain.model.valueobject.FlightNumber;
import com.coreorder.domain.model.valueobject.FlightSearchCriteria;
import com.coreorder.domain.model.valueobject.Money;
import com.coreorder.domain.port.out.FlightSearchProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlightSearchServiceTest {

    private FlightSearchService flightSearchService;
    private FakeFlightSearchProvider travelportProvider;
    private FakeFlightSearchProvider amadeusProvider;

    @BeforeEach
    void setUp() {
        travelportProvider = new FakeFlightSearchProvider(ProviderType.TRAVELPORT);
        amadeusProvider = new FakeFlightSearchProvider(ProviderType.AMADEUS);

        ProviderRouter router = new ProviderRouter(
                List.of(travelportProvider, amadeusProvider),
                List.of(),
                List.of()
        );

        flightSearchService = new FlightSearchService(router);
    }

    @Test
    void shouldSearchFlightsUsingPreferredProvider() {
        var command = new SearchFlightsCommand(
                "ONE_WAY",
                "CDG",
                "JFK",
                LocalDate.of(2026, 8, 15),
                null,
                1, 0, 0,
                "ECONOMY",
                50,
                List.of(),
                "TRAVELPORT"
        );

        var results = flightSearchService.searchFlights(command);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().providerId()).isEqualTo("TRAVELPORT");
        assertThat(travelportProvider.searchCalled).isTrue();
        assertThat(amadeusProvider.searchCalled).isFalse();
    }

    @Test
    void shouldSearchFlightsUsingAmadeusWhenPreferred() {
        var command = new SearchFlightsCommand(
                "ONE_WAY",
                "CDG",
                "JFK",
                LocalDate.of(2026, 8, 15),
                null,
                1, 0, 0,
                "ECONOMY",
                50,
                List.of(),
                "AMADEUS"
        );

        var results = flightSearchService.searchFlights(command);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().providerId()).isEqualTo("AMADEUS");
        assertThat(amadeusProvider.searchCalled).isTrue();
    }

    @Test
    void shouldThrowWhenNoProviderSpecified() {
        var command = new SearchFlightsCommand(
                "ONE_WAY",
                "CDG",
                "JFK",
                LocalDate.of(2026, 8, 15),
                null,
                1, 0, 0,
                "ECONOMY",
                50,
                List.of(),
                null
        );

        assertThatThrownBy(() -> flightSearchService.searchFlights(command))
                .isInstanceOf(ProviderNotFoundException.class);
    }

    @Test
    void shouldReturnAvailableProviders() {
        var providers = flightSearchService.getAvailableProviders();
        assertThat(providers).containsExactlyInAnyOrder("TRAVELPORT", "AMADEUS");
    }

    // --- Fake provider ---

    private static class FakeFlightSearchProvider implements FlightSearchProvider {
        private final ProviderType type;
        boolean searchCalled = false;

        FakeFlightSearchProvider(ProviderType type) {
            this.type = type;
        }

        @Override
        public ProviderType providerType() { return type; }

        @Override
        public List<FlightOffer> searchFlights(FlightSearchCriteria criteria) {
            searchCalled = true;
            return List.of(createSampleOffer());
        }

        @Override
        public boolean isEnabled() { return true; }

        private FlightOffer createSampleOffer() {
            var segment = new Segment(
                    FlightNumber.of(AirlineCode.of("AF"), "1234"),
                    AirportCode.of("CDG"), AirportCode.of("JFK"),
                    LocalDateTime.of(2026, 8, 15, 10, 0),
                    LocalDateTime.of(2026, 8, 15, 13, 0),
                    CabinClass.ECONOMY, BookingClass.of("Y"), 10,
                    Segment.AircraftInfo.of("77W", "Boeing 777-300ER")
            );
            var itinerary = new Itinerary(List.of(segment));
            return new FlightOffer(
                    "OFFER-" + type.name(), List.of(itinerary),
                    Money.of(500.00, "EUR"), Money.of(400.00, "EUR"), Money.of(100.00, "EUR"),
                    "AF", true, 9
            );
        }
    }
}
