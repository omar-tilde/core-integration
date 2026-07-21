package com.coreorder.domain.model.valueobject;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlightSearchCriteriaTest {

    @Test
    void shouldCreateValidOneWayCriteria() {
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

        assertThat(criteria.isOneWay()).isTrue();
        assertThat(criteria.isRoundTrip()).isFalse();
        assertThat(criteria.totalPassengers()).isEqualTo(1);
    }

    @Test
    void shouldCreateValidRoundTripCriteria() {
        var criteria = new FlightSearchCriteria(
                TripType.ROUND_TRIP,
                AirportCode.of("CDG"),
                AirportCode.of("JFK"),
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 8, 22),
                2, 1, 0,
                CabinClass.BUSINESS,
                100,
                List.of("AF", "DL")
        );

        assertThat(criteria.isOneWay()).isFalse();
        assertThat(criteria.isRoundTrip()).isTrue();
        assertThat(criteria.totalPassengers()).isEqualTo(3);
        assertThat(criteria.preferredAirlines()).containsExactly("AF", "DL");
    }

    @Test
    void shouldRejectReturnDateBeforeDeparture() {
        assertThatThrownBy(() -> new FlightSearchCriteria(
                TripType.ROUND_TRIP,
                AirportCode.of("CDG"),
                AirportCode.of("JFK"),
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 8, 10), // before departure
                1, 0, 0,
                CabinClass.ECONOMY,
                50,
                List.of()
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Return date must not be before departure date");
    }

    @Test
    void shouldRejectRoundTripWithoutReturnDate() {
        assertThatThrownBy(() -> new FlightSearchCriteria(
                TripType.ROUND_TRIP,
                AirportCode.of("CDG"),
                AirportCode.of("JFK"),
                LocalDate.of(2026, 8, 15),
                null, // missing return date
                1, 0, 0,
                CabinClass.ECONOMY,
                50,
                List.of()
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Return date is required for round-trip");
    }

    @Test
    void shouldRejectZeroAdults() {
        assertThatThrownBy(() -> new FlightSearchCriteria(
                TripType.ONE_WAY,
                AirportCode.of("CDG"),
                AirportCode.of("JFK"),
                LocalDate.of(2026, 8, 15),
                null,
                0, 0, 0, // no adults
                CabinClass.ECONOMY,
                50,
                List.of()
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("At least one adult");
    }

    @Test
    void shouldRejectNegativePassengerCounts() {
        assertThatThrownBy(() -> new FlightSearchCriteria(
                TripType.ONE_WAY,
                AirportCode.of("CDG"),
                AirportCode.of("JFK"),
                LocalDate.of(2026, 8, 15),
                null,
                1, -1, 0,
                CabinClass.ECONOMY,
                50,
                List.of()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldDefensivelyCopyPreferredAirlines() {
        var airlines = new ArrayList<>(List.of("AF"));
        var criteria = new FlightSearchCriteria(
                TripType.ONE_WAY,
                AirportCode.of("CDG"),
                AirportCode.of("JFK"),
                LocalDate.of(2026, 8, 15),
                null,
                1, 0, 0,
                CabinClass.ECONOMY,
                50,
                airlines
        );

        airlines.add("LH"); // mutating the original list
        assertThat(criteria.preferredAirlines()).containsExactly("AF"); // should not be affected
    }
}
