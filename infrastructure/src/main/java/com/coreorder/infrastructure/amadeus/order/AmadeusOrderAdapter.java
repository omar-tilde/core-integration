package com.coreorder.infrastructure.amadeus.order;

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
import com.coreorder.infrastructure.amadeus.base.AmadeusClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Amadeus implementation of the OrderManagementProvider port.
 */
@Component
@ConditionalOnProperty(prefix = "providers.amadeus", name = "enabled", havingValue = "true")
public class AmadeusOrderAdapter implements OrderManagementProvider {

    private final AmadeusClient client;

    public AmadeusOrderAdapter(AmadeusClient client) {
        this.client = client;
    }

    @Override
    public ProviderType providerType() {
        return ProviderType.AMADEUS;
    }

    @Override
    public Order createOrder(OrderCreateRequest request) {
        try {
            return buildMockOrder(request);
        } catch (RuntimeException e) {
            throw new ProviderCommunicationException(
                    "Failed to create order via Amadeus: " + e.getMessage(), e);
        }
    }

    @Override
    public Order retrieveOrder(String providerOrderId) {
        try {
            return buildMockRetrievedOrder(providerOrderId);
        } catch (RuntimeException e) {
            throw new ProviderCommunicationException(
                    "Failed to retrieve order from Amadeus: " + e.getMessage(), e);
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
                    "Failed to cancel order via Amadeus: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isEnabled() {
        return client.isEnabled();
    }

    private Order buildMockOrder(OrderCreateRequest request) {
        String orderId = "AMA-ORD-" + System.currentTimeMillis();
        return new Order(
                orderId,
                "AMA-" + request.offerId(),
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
                "P-AMA-1", "Jane", "Smith",
                java.time.LocalDate.of(1985, 5, 10),
                PassengerType.ADULT, null
        );

        return new Order(
                "AMA-ORD-RETRIEVED",
                providerOrderId,
                providerId(),
                List.of(passenger),
                List.of(createSampleOffer()),
                Money.of(475.00, "EUR"),
                OrderStatus.CONFIRMED,
                Instant.now().minusSeconds(7200)
        );
    }

    private FlightOffer createSampleOffer() {
        var segment = new Segment(
                FlightNumber.of(AirlineCode.of("AF"), "6001"),
                AirportCode.of("CDG"), AirportCode.of("JFK"),
                LocalDateTime.of(2026, 8, 15, 9, 0),
                LocalDateTime.of(2026, 8, 15, 12, 45),
                CabinClass.ECONOMY, BookingClass.of("Y"), 12,
                Segment.AircraftInfo.of("359", "Airbus A350-900")
        );
        return new FlightOffer(
                "AMA-OFFER-SAMPLE", List.of(new Itinerary(List.of(segment))),
                Money.of(475.00, "EUR"), Money.of(370.00, "EUR"), Money.of(105.00, "EUR"),
                "AF", true, 12
        );
    }
}
