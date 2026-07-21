package com.coreorder.domain.model.entity;

import com.coreorder.domain.model.enums.OrderStatus;
import com.coreorder.domain.model.valueobject.AirlineCode;
import com.coreorder.domain.model.valueobject.AirportCode;
import com.coreorder.domain.model.valueobject.BookingClass;
import com.coreorder.domain.model.valueobject.CabinClass;
import com.coreorder.domain.model.valueobject.FlightNumber;
import com.coreorder.domain.model.valueobject.Money;
import com.coreorder.domain.model.valueobject.PassengerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        var passenger = new Passenger(
                "P1", "John", "Doe",
                LocalDate.of(1990, 1, 1),
                PassengerType.ADULT,
                null
        );

        var segment = new Segment(
                FlightNumber.of(AirlineCode.of("AF"), "1234"),
                AirportCode.of("CDG"), AirportCode.of("JFK"),
                LocalDateTime.of(2026, 8, 15, 10, 0),
                LocalDateTime.of(2026, 8, 15, 13, 0),
                CabinClass.ECONOMY, BookingClass.of("Y"), 10,
                Segment.AircraftInfo.of("77W", "Boeing 777-300ER")
        );
        var itinerary = new Itinerary(List.of(segment));
        var offer = new FlightOffer(
                "OFFER-1", List.of(itinerary),
                Money.of(500.00, "EUR"), Money.of(400.00, "EUR"), Money.of(100.00, "EUR"),
                "AF", true, 9
        );

        order = new Order(
                "ORD-1", "PROV-123", "TRAVELPORT",
                List.of(passenger), List.of(offer),
                Money.of(500.00, "EUR"),
                OrderStatus.PENDING,
                Instant.now()
        );
    }

    @Test
    void shouldCreatePendingOrder() {
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.isActive()).isTrue();
        assertThat(order.passengerCount()).isEqualTo(1);
    }

    @Test
    void shouldConfirmPendingOrder() {
        order.confirm();
        assertThat(order.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.isActive()).isTrue();
    }

    @Test
    void shouldTicketConfirmedOrder() {
        order.confirm();
        order.ticket();
        assertThat(order.status()).isEqualTo(OrderStatus.TICKETED);
        assertThat(order.ticketedAt()).isNotNull();
        assertThat(order.isActive()).isTrue();
    }

    @Test
    void shouldCancelPendingOrder() {
        order.cancel();
        assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.isActive()).isFalse();
    }

    @Test
    void shouldCancelConfirmedOrder() {
        order.confirm();
        order.cancel();
        assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldRefundTicketedOrder() {
        order.confirm();
        order.ticket();
        order.refund();
        assertThat(order.status()).isEqualTo(OrderStatus.REFUNDED);
        assertThat(order.isActive()).isFalse();
    }

    @Test
    void shouldNotConfirmAlreadyConfirmedOrder() {
        order.confirm();
        assertThatThrownBy(order::confirm)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only pending orders can be confirmed");
    }

    @Test
    void shouldNotTicketPendingOrder() {
        assertThatThrownBy(order::ticket)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only confirmed orders can be ticketed");
    }

    @Test
    void shouldNotCancelTicketedOrder() {
        order.confirm();
        order.ticket();
        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel a ticketed order");
    }

    @Test
    void shouldNotRefundNonTicketedOrder() {
        assertThatThrownBy(order::refund)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only ticketed orders can be refunded");
    }

    @Test
    void shouldNotCancelAlreadyCancelledOrder() {
        order.cancel();
        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    void shouldRejectOrderWithNoPassengers() {
        assertThatThrownBy(() -> new Order(
                "ORD-2", "PROV-456", "AMADEUS",
                List.of(), List.of(),
                Money.of(500.00, "EUR"),
                OrderStatus.PENDING,
                Instant.now()
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one passenger");
    }

    @Test
    void shouldRejectOrderWithNoOffers() {
        var passenger = new Passenger("P1", "John", "Doe",
                LocalDate.of(1990, 1, 1), PassengerType.ADULT, null);
        assertThatThrownBy(() -> new Order(
                "ORD-2", "PROV-456", "AMADEUS",
                List.of(passenger), List.of(),
                Money.of(500.00, "EUR"),
                OrderStatus.PENDING,
                Instant.now()
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one offer");
    }

    @Test
    void shouldReturnImmutablePassengers() {
        var passengers = order.passengers();
        assertThatThrownBy(() -> passengers.add(
                new Passenger("P2", "Jane", "Smith", LocalDate.of(1995, 1, 1),
                        PassengerType.ADULT, null)
        )).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldTrackUpdatedAtTimestamp() {
        var originalUpdatedAt = order.updatedAt();
        order.confirm();
        assertThat(order.updatedAt()).isAfterOrEqualTo(originalUpdatedAt);
    }
}
