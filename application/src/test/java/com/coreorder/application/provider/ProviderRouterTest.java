package com.coreorder.application.provider;

import com.coreorder.application.base.exception.ProviderNotFoundException;
import com.coreorder.application.base.exception.ProviderUnavailableException;
import com.coreorder.application.base.provider.ProviderRouter;
import com.coreorder.domain.model.entity.FlightOffer;
import com.coreorder.domain.model.entity.Order;
import com.coreorder.domain.model.enums.ProviderType;
import com.coreorder.domain.model.valueobject.FlightSearchCriteria;
import com.coreorder.domain.port.out.FlightSearchProvider;
import com.coreorder.domain.port.out.OrderManagementProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProviderRouterTest {

    private ProviderRouter router;

    @BeforeEach
    void setUp() {
        router = new ProviderRouter(
                List.of(new FakeFlightSearchProvider(ProviderType.TRAVELPORT, true),
                        new FakeFlightSearchProvider(ProviderType.AMADEUS, true),
                        new FakeFlightSearchProvider(ProviderType.SABRE, false)),
                List.of(new FakeOrderManagementProvider(ProviderType.TRAVELPORT, true),
                        new FakeOrderManagementProvider(ProviderType.AMADEUS, true)),
                List.of()
        );
    }

    @Test
    void shouldResolveSpecificFlightSearchProviderByEnum() {
        var provider = router.resolveFlightSearchProvider(ProviderType.TRAVELPORT);
        assertThat(provider.providerType()).isEqualTo(ProviderType.TRAVELPORT);
        assertThat(provider.providerId()).isEqualTo("TRAVELPORT");
    }

    @Test
    void shouldResolveAmadeusProviderByString() {
        var provider = router.resolveFlightSearchProvider("AMADEUS");
        assertThat(provider.providerType()).isEqualTo(ProviderType.AMADEUS);
        assertThat(provider.providerId()).isEqualTo("AMADEUS");
    }

    @Test
    void shouldResolveFirstAvailableWhenNoPreference() {
        var provider = router.resolveFlightSearchProvider((String) null);
        assertThat(provider).isNotNull();
        assertThat(provider.isAvailable()).isTrue();
    }

    @Test
    void shouldThrowWhenProviderNotFoundByStringCode() {
        assertThatThrownBy(() -> router.resolveFlightSearchProvider("NONEXISTENT"))
                .isInstanceOf(ProviderNotFoundException.class);
    }

    @Test
    void shouldThrowWhenProviderUnavailable() {
        assertThatThrownBy(() -> router.resolveFlightSearchProvider(ProviderType.SABRE))
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
        assertThat(provider.providerType()).isEqualTo(ProviderType.TRAVELPORT);
    }

    @Test
    void shouldThrowWhenOrderProviderNotFound() {
        assertThatThrownBy(() -> router.getOrderManagementProvider(ProviderType.SABRE))
                .isInstanceOf(ProviderNotFoundException.class);
    }

    // --- Fake providers for testing ---

    private record FakeFlightSearchProvider(ProviderType type, boolean available) implements FlightSearchProvider {
        @Override
        public ProviderType providerType() { return type; }

        @Override
        public List<FlightOffer> searchFlights(FlightSearchCriteria criteria) {
            return List.of();
        }

        @Override
        public boolean isAvailable() { return available; }
    }

    private record FakeOrderManagementProvider(ProviderType type, boolean available) implements OrderManagementProvider {
        @Override
        public ProviderType providerType() { return type; }

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
