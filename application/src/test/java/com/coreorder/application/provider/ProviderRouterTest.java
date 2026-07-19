package com.coreorder.application.provider;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.coreorder.domain.model.entity.FlightOffer;
import com.coreorder.domain.model.entity.Order;
import com.coreorder.domain.model.valueobject.FlightSearchCriteria;
import com.coreorder.domain.port.out.FlightSearchProvider;
import com.coreorder.domain.port.out.OrderManagementProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProviderRouterTest {

    private ProviderRouter router;

    @BeforeEach
    void setUp() {
        router = new ProviderRouter(
                List.of(new FakeFlightSearchProvider("TRAVELPORT", true),
                        new FakeFlightSearchProvider("AMADEUS", true),
                        new FakeFlightSearchProvider("SABRE", false)),
                List.of(new FakeOrderManagementProvider("TRAVELPORT", true),
                        new FakeOrderManagementProvider("AMADEUS", true)),
                List.of()
        );
    }

    @Test
    void shouldResolveSpecificFlightSearchProvider() {
        var provider = router.resolveFlightSearchProvider("TRAVELPORT");
        assertThat(provider.providerId()).isEqualTo("TRAVELPORT");
    }

    @Test
    void shouldResolveAmadeusProvider() {
        var provider = router.resolveFlightSearchProvider("AMADEUS");
        assertThat(provider.providerId()).isEqualTo("AMADEUS");
    }

    @Test
    void shouldResolveFirstAvailableWhenNoPreference() {
        var provider = router.resolveFlightSearchProvider(null);
        assertThat(provider).isNotNull();
        assertThat(provider.isAvailable()).isTrue();
    }

    @Test
    void shouldThrowWhenProviderNotFound() {
        assertThatThrownBy(() -> router.resolveFlightSearchProvider("NONEXISTENT"))
                .isInstanceOf(ProviderNotFoundException.class)
                .hasMessageContaining("NONEXISTENT");
    }

    @Test
    void shouldThrowWhenProviderUnavailable() {
        assertThatThrownBy(() -> router.resolveFlightSearchProvider("SABRE"))
                .isInstanceOf(ProviderUnavailableException.class)
                .hasMessageContaining("SABRE");
    }

    @Test
    void shouldGetAvailableProviderIds() {
        var ids = router.getAvailableFlightSearchProviderIds();
        assertThat(ids).containsExactlyInAnyOrder("TRAVELPORT", "AMADEUS");
    }

    @Test
    void shouldResolveOrderManagementProvider() {
        var provider = router.resolveOrderManagementProvider("TRAVELPORT");
        assertThat(provider.providerId()).isEqualTo("TRAVELPORT");
    }

    @Test
    void shouldThrowWhenOrderProviderNotFound() {
        assertThatThrownBy(() -> router.getOrderManagementProvider("NONEXISTENT"))
                .isInstanceOf(ProviderNotFoundException.class);
    }

    // --- Fake providers for testing ---

    private record FakeFlightSearchProvider(String id, boolean available) implements FlightSearchProvider {
        @Override
        public String providerId() { return id; }

        @Override
        public List<FlightOffer> searchFlights(FlightSearchCriteria criteria) {
            return List.of();
        }

        @Override
        public boolean isAvailable() { return available; }
    }

    private record FakeOrderManagementProvider(String id, boolean available) implements OrderManagementProvider {
        @Override
        public String providerId() { return id; }

        @Override
        public Order createOrder(OrderCreateRequest request) { return null; }

        @Override
        public Order retrieveOrder(String providerOrderId) { return null; }

        @Override
        public Order cancelOrder(String providerOrderId) { return null; }

        @Override
        public boolean isAvailable() { return available; }
    }
}
