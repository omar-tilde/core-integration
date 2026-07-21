package com.coreorder.application.base.provider;

import com.coreorder.domain.model.enums.ProviderType;
import com.coreorder.domain.port.out.FlightSearchProvider;
import com.coreorder.domain.port.out.OrderManagementProvider;
import com.coreorder.domain.port.out.PricingProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Routes requests to the appropriate aviation provider using strongly-typed strategies.
 * <p>
 * This class holds strategy registries for all registered provider adapters.
 * Spring automatically injects all active provider beans into this component.
 */
@Component
public class ProviderRouter {

    private final ProviderStrategyRegistry<FlightSearchProvider> flightSearchRegistry;
    private final ProviderStrategyRegistry<OrderManagementProvider> orderManagementRegistry;
    private final ProviderStrategyRegistry<PricingProvider> pricingRegistry;

    public ProviderRouter(
            List<FlightSearchProvider> flightSearchProviders,
            List<OrderManagementProvider> orderManagementProviders,
            List<PricingProvider> pricingProviders
    ) {
        this.flightSearchRegistry = new ProviderStrategyRegistry<>(flightSearchProviders, "flight search provider");
        this.orderManagementRegistry = new ProviderStrategyRegistry<>(orderManagementProviders, "order management provider");
        this.pricingRegistry = new ProviderStrategyRegistry<>(pricingProviders, "pricing provider");
    }

    // --- Flight Search Strategy Resolution ---

    public FlightSearchProvider getFlightSearchProvider(ProviderType providerType) {
        return flightSearchRegistry.getProvider(providerType);
    }

    public FlightSearchProvider getFlightSearchProvider(String providerId) {
        return flightSearchRegistry.getProvider(providerId);
    }

    public FlightSearchProvider resolveFlightSearchProvider(ProviderType preferredType) {
        return flightSearchRegistry.resolveProvider(preferredType);
    }

    public FlightSearchProvider resolveFlightSearchProvider(String preferredProviderId) {
        return flightSearchRegistry.resolveProvider(preferredProviderId);
    }

    public List<String> getAvailableFlightSearchProviderIds() {
        return flightSearchRegistry.getAvailableProviderIds();
    }

    public List<ProviderType> getAvailableFlightSearchProviderTypes() {
        return flightSearchRegistry.getAvailableProviderTypes();
    }

    // --- Order Management Strategy Resolution ---

    public OrderManagementProvider getOrderManagementProvider(ProviderType providerType) {
        return orderManagementRegistry.getProvider(providerType);
    }

    public OrderManagementProvider getOrderManagementProvider(String providerId) {
        return orderManagementRegistry.getProvider(providerId);
    }

    public OrderManagementProvider resolveOrderManagementProvider(ProviderType preferredType) {
        return orderManagementRegistry.resolveProvider(preferredType);
    }

    public OrderManagementProvider resolveOrderManagementProvider(String preferredProviderId) {
        return orderManagementRegistry.resolveProvider(preferredProviderId);
    }

    public List<String> getAvailableOrderManagementProviderIds() {
        return orderManagementRegistry.getAvailableProviderIds();
    }

    public List<ProviderType> getAvailableOrderManagementProviderTypes() {
        return orderManagementRegistry.getAvailableProviderTypes();
    }

    // --- Pricing Strategy Resolution ---

    public PricingProvider getPricingProvider(ProviderType providerType) {
        return pricingRegistry.getProvider(providerType);
    }

    public PricingProvider getPricingProvider(String providerId) {
        return pricingRegistry.getProvider(providerId);
    }

    public PricingProvider resolvePricingProvider(ProviderType preferredType) {
        return pricingRegistry.resolveProvider(preferredType);
    }

    public PricingProvider resolvePricingProvider(String preferredProviderId) {
        return pricingRegistry.resolveProvider(preferredProviderId);
    }
}
