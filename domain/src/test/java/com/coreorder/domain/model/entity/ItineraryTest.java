package com.coreorder.domain.model.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.coreorder.domain.model.valueobject.AirlineCode;
import com.coreorder.domain.model.valueobject.AirportCode;
import com.coreorder.domain.model.valueobject.BookingClass;
import com.coreorder.domain.model.valueobject.CabinClass;
import com.coreorder.domain.model.valueobject.FlightNumber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItineraryTest {

    private static final AirportCode CDG = AirportCode.of("CDG");
    private static final AirportCode JFK = AirportCode.of("JFK");
    private static final AirportCode LHR = AirportCode.of("LHR");
    private static final Segment.AircraftInfo AIRCRAFT = Segment.AircraftInfo.of("77W", "Boeing 777-300ER");

    private Segment directSegment() {
        return new Segment(
                FlightNumber.of(AirlineCode.of("AF"), "1234"),
                CDG, JFK,
                LocalDateTime.of(2026, 8, 15, 10, 0),
                LocalDateTime.of(2026, 8, 15, 13, 0),
                CabinClass.ECONOMY, BookingClass.of("Y"), 10, AIRCRAFT
        );
    }

    private Segment firstConnectingSegment() {
        return new Segment(
                FlightNumber.of(AirlineCode.of("AF"), "100"),
                CDG, LHR,
                LocalDateTime.of(2026, 8, 15, 8, 0),
                LocalDateTime.of(2026, 8, 15, 9, 30),
                CabinClass.ECONOMY, BookingClass.of("Y"), 15, AIRCRAFT
        );
    }

    private Segment secondConnectingSegment() {
        return new Segment(
                FlightNumber.of(AirlineCode.of("BA"), "200"),
                LHR, JFK,
                LocalDateTime.of(2026, 8, 15, 11, 0),
                LocalDateTime.of(2026, 8, 15, 14, 0),
                CabinClass.ECONOMY, BookingClass.of("Y"), 8, AIRCRAFT
        );
    }

    @Test
    void shouldCreateDirectItinerary() {
        var itinerary = new Itinerary(List.of(directSegment()));

        assertThat(itinerary.isDirect()).isTrue();
        assertThat(itinerary.isConnecting()).isFalse();
        assertThat(itinerary.numberOfStops()).isZero();
        assertThat(itinerary.origin()).isEqualTo(CDG);
        assertThat(itinerary.destination()).isEqualTo(JFK);
        assertThat(itinerary.totalDuration().toHours()).isEqualTo(3);
    }

    @Test
    void shouldCreateConnectingItinerary() {
        var itinerary = new Itinerary(List.of(firstConnectingSegment(), secondConnectingSegment()));

        assertThat(itinerary.isDirect()).isFalse();
        assertThat(itinerary.isConnecting()).isTrue();
        assertThat(itinerary.numberOfStops()).isEqualTo(1);
        assertThat(itinerary.origin()).isEqualTo(CDG);
        assertThat(itinerary.destination()).isEqualTo(JFK);
        // Total: 08:00 to 14:00 = 6 hours
        assertThat(itinerary.totalDuration().toHours()).isEqualTo(6);
        // Flight time: 1.5h + 3h = 4.5h, layover = 1.5h
        assertThat(itinerary.totalLayoverDuration().toMinutes()).isEqualTo(90);
    }

    @Test
    void shouldRejectEmptySegments() {
        assertThatThrownBy(() -> new Itinerary(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one segment");
    }

    @Test
    void shouldRejectDiscontinuousSegments() {
        // Create a segment from LHR to CDG (wrong connection — CDG != LHR expected)
        var wrongSecond = new Segment(
                FlightNumber.of(AirlineCode.of("BA"), "200"),
                CDG, JFK, // origin is CDG but first segment arrives at LHR
                LocalDateTime.of(2026, 8, 15, 11, 0),
                LocalDateTime.of(2026, 8, 15, 14, 0),
                CabinClass.ECONOMY, BookingClass.of("Y"), 8, AIRCRAFT
        );

        assertThatThrownBy(() -> new Itinerary(List.of(firstConnectingSegment(), wrongSecond)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Segment discontinuity");
    }

    @Test
    void shouldRejectSegmentsOutOfOrder() {
        // Both depart from CDG but second departs before first arrives
        var seg1 = new Segment(
                FlightNumber.of(AirlineCode.of("AF"), "100"),
                CDG, LHR,
                LocalDateTime.of(2026, 8, 15, 10, 0),
                LocalDateTime.of(2026, 8, 15, 12, 0),
                CabinClass.ECONOMY, BookingClass.of("Y"), 15, AIRCRAFT
        );
        var seg2 = new Segment(
                FlightNumber.of(AirlineCode.of("BA"), "200"),
                LHR, JFK,
                LocalDateTime.of(2026, 8, 15, 11, 0), // before seg1 arrives!
                LocalDateTime.of(2026, 8, 15, 14, 0),
                CabinClass.ECONOMY, BookingClass.of("Y"), 8, AIRCRAFT
        );

        assertThatThrownBy(() -> new Itinerary(List.of(seg1, seg2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("departs before");
    }
}
