package com.coreorder.application.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.coreorder.application.provider.ProviderRouter;
import com.coreorder.application.service.FlightSearchService;
import com.coreorder.application.service.OrderService;
import com.coreorder.domain.port.out.FlightSearchProvider;
import com.coreorder.domain.port.out.OrderManagementProvider;
import com.coreorder.domain.port.out.PricingProvider;

/**
 * Spring configuration for the application layer.
 * Wires together the provider router and application services.
 */
@Configuration
public class ApplicationConfiguration {

    @Bean
    public ProviderRouter providerRouter(
            List<FlightSearchProvider> flightSearchProviders,
            List<OrderManagementProvider> orderManagementProviders,
            List<PricingProvider> pricingProviders
    ) {
        return new ProviderRouter(flightSearchProviders, orderManagementProviders, pricingProviders);
    }

    @Bean
    public FlightSearchService flightSearchService(ProviderRouter providerRouter) {
        return new FlightSearchService(providerRouter);
    }

    @Bean
    public OrderService orderService(ProviderRouter providerRouter) {
        return new OrderService(providerRouter);
    }
}
