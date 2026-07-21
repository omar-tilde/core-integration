package com.coreorder.infrastructure.travelport.order;

import com.coreorder.domain.exception.ProviderCommunicationException;
import com.coreorder.domain.model.entity.FlightOffer;
import com.coreorder.domain.model.entity.Itinerary;
import com.coreorder.domain.model.entity.Order;
import com.coreorder.domain.model.entity.Passenger;
import com.coreorder.domain.model.entity.Segment;
import com.coreorder.domain.model.enums.OrderStatus;
import com.coreorder.domain.model.enums.ProviderType;
import com.coreorder.domain.model.valueobject.AirlineCode;
import com.coreorder.domain.model.valueobject.AirportCode;
import com.coreorder.domain.model.valueobject.BookingClass;
import com.coreorder.domain.model.valueobject.CabinClass;
import com.coreorder.domain.model.valueobject.FlightNumber;
import com.coreorder.domain.model.valueobject.Money;
import com.coreorder.domain.model.valueobject.PassengerType;
import com.coreorder.domain.port.out.OrderManagementProvider;
import com.coreorder.infrastructure.travelport.base.TravelportClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Travelport implementation of the OrderManagementProvider port.
 */
@Component
@ConditionalOnProperty(prefix = "providers.travelport", name = "enabled", havingValue = "true")
public class TravelportOrderAdapter implements OrderManagementProvider {

    private final TravelportClient client;

    public TravelportOrderAdapter(TravelportClient client) {
        this.client = client;
    }

    @Override
    public ProviderType providerType() {
        return ProviderType.TRAVELPORT;
    }

    @Override
    public Order createOrder(OrderCreateRequest request) {
        try {
            return buildMockOrder(request);
        } catch (RuntimeException e) {
            throw new ProviderCommunicationException(
                    "Failed to create order via Travelport: " + e.getMessage(), e);
        }
    }

    @Override
    public Order retrieveOrder(String providerOrderId) {
        try {
            return buildMockRetrievedOrder(providerOrderId);
        } catch (RuntimeException e) {
            throw new ProviderCommunicationException(
                    "Failed to retrieve order from Travelport: " + e.getMessage(), e);
        }
    }

    @Override
    public Order cancelOrder(String providerOrderId) {
        try {
            var order = buildMockRetrievedOrder(providerOrderId);
            order.cancel();
            return order;
        } catch (RuntimeException e) {
            throw new ProviderCommunicationException(
                    "Failed to cancel order via Travelport: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        return client.isReachable();
    }

    private Order buildMockOrder(OrderCreateRequest request) {
        String orderId = "TP-ORD-" + System.currentTimeMillis();
        return new Order(
                orderId,
                "TP-" + request.offerId(),
                providerId(),
                request.passengers(),
                List.of(createSampleOffer()),
                request.expectedTotalPrice(),
                OrderStatus.PENDING,
                Instant.now()
        );
    }

    private Order buildMockRetrievedOrder(String providerOrderId) {
        var passenger = new Passenger(
                "P-RETRIEVED-1", "John", "Doe",
                java.time.LocalDate.of(1990, 1, 1),
                PassengerType.ADULT, null
        );

        return new Order(
                "TP-ORD-RETRIEVED",
                providerOrderId,
                providerId(),
                List.of(passenger),
                List.of(createSampleOffer()),
                Money.of(500.00, "EUR"),
                OrderStatus.CONFIRMED,
                Instant.now().minusSeconds(3600)
        );
    }

    private FlightOffer createSampleOffer() {
        var segment = new Segment(
                FlightNumber.of(AirlineCode.of("AF"), "1234"),
                AirportCode.of("CDG"), AirportCode.of("JFK"),
                LocalDateTime.of(2026, 8, 15, 10, 0),
                LocalDateTime.of(2026, 8, 15, 13, 30),
                CabinClass.ECONOMY, BookingClass.of("Y"), 10,
                Segment.AircraftInfo.of("77W", "Boeing 777-300ER")
        );
        return new FlightOffer(
                "TP-OFFER-SAMPLE", List.of(new Itinerary(List.of(segment))),
                Money.of(500.00, "EUR"), Money.of(400.00, "EUR"), Money.of(100.00, "EUR"),
                "AF", true, 10
        );
    }
}
