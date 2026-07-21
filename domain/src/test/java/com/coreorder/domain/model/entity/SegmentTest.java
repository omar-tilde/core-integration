package com.coreorder.domain.model.entity;

import com.coreorder.domain.model.valueobject.AirlineCode;
import com.coreorder.domain.model.valueobject.AirportCode;
import com.coreorder.domain.model.valueobject.BookingClass;
import com.coreorder.domain.model.valueobject.CabinClass;
import com.coreorder.domain.model.valueobject.FlightNumber;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SegmentTest {

    private static final FlightNumber FLIGHT_NUMBER = FlightNumber.of(AirlineCode.of("AF"), "1234");
    private static final AirportCode CDG = AirportCode.of("CDG");
    private static final AirportCode JFK = AirportCode.of("JFK");
    private static final LocalDateTime DEPARTURE = LocalDateTime.of(2026, 8, 15, 10, 0);
    private static final LocalDateTime ARRIVAL = LocalDateTime.of(2026, 8, 15, 13, 0);
    private static final Segment.AircraftInfo AIRCRAFT = Segment.AircraftInfo.of("77W", "Boeing 777-300ER");

    @Test
    void shouldCreateValidSegment() {
        var segment = new Segment(FLIGHT_NUMBER, CDG, JFK, DEPARTURE, ARRIVAL,
                CabinClass.ECONOMY, BookingClass.of("Y"), 10, AIRCRAFT);

        assertThat(segment.flightNumber()).isEqualTo(FLIGHT_NUMBER);
        assertThat(segment.origin()).isEqualTo(CDG);
        assertThat(segment.destination()).isEqualTo(JFK);
        assertThat(segment.duration().toHours()).isEqualTo(3);
    }

    @Test
    void shouldRejectArrivalBeforeDeparture() {
        assertThatThrownBy(() -> new Segment(FLIGHT_NUMBER, CDG, JFK, ARRIVAL, DEPARTURE,
                CabinClass.ECONOMY, BookingClass.of("Y"), 10, AIRCRAFT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Arrival time must be after departure time");
    }

    @Test
    void shouldRejectNegativeSeats() {
        assertThatThrownBy(() -> new Segment(FLIGHT_NUMBER, CDG, JFK, DEPARTURE, ARRIVAL,
                CabinClass.ECONOMY, BookingClass.of("Y"), -1, AIRCRAFT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Available seats must not be negative");
    }

    @Test
    void shouldRejectNullOrigin() {
        assertThatThrownBy(() -> new Segment(FLIGHT_NUMBER, null, JFK, DEPARTURE, ARRIVAL,
                CabinClass.ECONOMY, BookingClass.of("Y"), 10, AIRCRAFT))
                .isInstanceOf(NullPointerException.class);
    }
}
