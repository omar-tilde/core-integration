package com.coreorder.application.provider;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.coreorder.domain.port.out.FlightSearchProvider;
import com.coreorder.domain.port.out.OrderManagementProvider;
import com.coreorder.domain.port.out.PricingProvider;

/**
 * Routes requests to the appropriate aviation provider.
 * <p>
 * This class holds all registered provider adapters and provides lookup
 * by provider ID. It implements a strategy pattern for provider selection.
 * <p>
 * When a preferred provider is specified, that provider is used.
 * When no preference is given, the first available provider is selected.
 */
public class ProviderRouter {

    private final Map<String, FlightSearchProvider> flightSearchProviders;
    private final Map<String, OrderManagementProvider> orderManagementProviders;
    private final Map<String, PricingProvider> pricingProviders;

    public ProviderRouter(
            List<FlightSearchProvider> flightSearchProviders,
            List<OrderManagementProvider> orderManagementProviders,
            List<PricingProvider> pricingProviders
    ) {
        this.flightSearchProviders = flightSearchProviders.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toMap(FlightSearchProvider::providerId, Function.identity()));
        this.orderManagementProviders = orderManagementProviders.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toMap(OrderManagementProvider::providerId, Function.identity()));
        this.pricingProviders = pricingProviders.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toMap(PricingProvider::providerId, Function.identity()));
    }

    /**
     * Get a specific flight search provider by ID.
     */
    public FlightSearchProvider getFlightSearchProvider(String providerId) {
        return Optional.ofNullable(flightSearchProviders.get(providerId))
                .orElseThrow(() -> new ProviderNotFoundException(
                        "No flight search provider found with ID: '%s'".formatted(providerId)));
    }

    /**
     * Get the first available flight search provider.
     */
    public FlightSearchProvider getFirstAvailableFlightSearchProvider() {
        return flightSearchProviders.values().stream()
                .filter(FlightSearchProvider::isAvailable)
                .findFirst()
                .orElseThrow(() -> new NoProviderAvailableException(
                        "No flight search provider is currently available"));
    }

    /**
     * Resolve a flight search provider — uses the preferred one if specified and available,
     * otherwise falls back to the first available provider.
     */
    public FlightSearchProvider resolveFlightSearchProvider(String preferredProviderId) {
        if (preferredProviderId != null && !preferredProviderId.isBlank()) {
            FlightSearchProvider provider = flightSearchProviders.get(preferredProviderId);
            if (provider == null) {
                throw new ProviderNotFoundException(
                        "Flight search provider '%s' not found".formatted(preferredProviderId));
            }
            if (!provider.isAvailable()) {
                throw new ProviderUnavailableException(
                        "Flight search provider '%s' is currently unavailable".formatted(preferredProviderId));
            }
            return provider;
        }
        return getFirstAvailableFlightSearchProvider();
    }

    /**
     * Get a specific order management provider by ID.
     */
    public OrderManagementProvider getOrderManagementProvider(String providerId) {
        return Optional.ofNullable(orderManagementProviders.get(providerId))
                .orElseThrow(() -> new ProviderNotFoundException(
                        "No order management provider found with ID: '%s'".formatted(providerId)));
    }

    /**
     * Get the first available order management provider.
     */
    public OrderManagementProvider getFirstAvailableOrderManagementProvider() {
        return orderManagementProviders.values().stream()
                .filter(OrderManagementProvider::isAvailable)
                .findFirst()
                .orElseThrow(() -> new NoProviderAvailableException(
                        "No order management provider is currently available"));
    }

    /**
     * Resolve an order management provider.
     */
    public OrderManagementProvider resolveOrderManagementProvider(String preferredProviderId) {
        if (preferredProviderId != null && !preferredProviderId.isBlank()) {
            OrderManagementProvider provider = orderManagementProviders.get(preferredProviderId);
            if (provider == null) {
                throw new ProviderNotFoundException(
                        "Order management provider '%s' not found".formatted(preferredProviderId));
            }
            if (!provider.isAvailable()) {
                throw new ProviderUnavailableException(
                        "Order management provider '%s' is currently unavailable".formatted(preferredProviderId));
            }
            return provider;
        }
        return getFirstAvailableOrderManagementProvider();
    }

    /**
     * Get all registered flight search provider IDs.
     */
    public List<String> getAvailableFlightSearchProviderIds() {
        return flightSearchProviders.values().stream()
                .filter(FlightSearchProvider::isAvailable)
                .map(FlightSearchProvider::providerId)
                .toList();
    }

    /**
     * Get all registered order management provider IDs.
     */
    public List<String> getAvailableOrderManagementProviderIds() {
        return orderManagementProviders.values().stream()
                .filter(OrderManagementProvider::isAvailable)
                .map(OrderManagementProvider::providerId)
                .toList();
    }
}
