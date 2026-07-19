package com.coreorder.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.coreorder.domain.port.out.FlightSearchProvider;
import com.coreorder.domain.port.out.OrderManagementProvider;
import com.coreorder.domain.port.out.PricingProvider;
import com.coreorder.infrastructure.provider.amadeus.AmadeusClient;
import com.coreorder.infrastructure.provider.amadeus.AmadeusFlightSearchAdapter;
import com.coreorder.infrastructure.provider.amadeus.AmadeusOrderAdapter;
import com.coreorder.infrastructure.provider.amadeus.AmadeusPricingAdapter;
import com.coreorder.infrastructure.provider.amadeus.AmadeusProperties;
import com.coreorder.infrastructure.provider.travelport.TravelportClient;
import com.coreorder.infrastructure.provider.travelport.TravelportFlightSearchAdapter;
import com.coreorder.infrastructure.provider.travelport.TravelportOrderAdapter;
import com.coreorder.infrastructure.provider.travelport.TravelportPricingAdapter;
import com.coreorder.infrastructure.provider.travelport.TravelportProperties;

/**
 * Spring auto-configuration for provider infrastructure.
 * <p>
 * Each provider's beans are guarded by {@code providers.<id>.enabled} so that turning a
 * provider off in {@code application.yml} cleanly removes it from the application context.
 * <p>
 * Adding a new provider (e.g. Sabre) is a one-package operation:
 * <ol>
 *     <li>Create the adapter classes that implement the outbound ports</li>
 *     <li>Add a {@code @ConfigurationProperties} record</li>
 *     <li>Register the adapters here, guarded by a {@code @ConditionalOnProperty}</li>
 * </ol>
 * No changes to domain, application or presentation layers are required.
 */
@Configuration
@EnableConfigurationProperties({TravelportProperties.class, AmadeusProperties.class})
public class ProviderInfrastructureConfiguration {

    // ---------------------------------------------------------------------
    //  Travelport
    // ---------------------------------------------------------------------

    @Bean
    @ConditionalOnProperty(prefix = "providers.travelport", name = "enabled", havingValue = "true")
    public TravelportClient travelportClient(TravelportProperties properties) {
        return new TravelportClient(properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "providers.travelport", name = "enabled", havingValue = "true")
    public FlightSearchProvider travelportFlightSearchAdapter(TravelportClient client) {
        return new TravelportFlightSearchAdapter(client);
    }

    @Bean
    @ConditionalOnProperty(prefix = "providers.travelport", name = "enabled", havingValue = "true")
    public OrderManagementProvider travelportOrderAdapter(TravelportClient client) {
        return new TravelportOrderAdapter(client);
    }

    @Bean
    @ConditionalOnProperty(prefix = "providers.travelport", name = "enabled", havingValue = "true")
    public PricingProvider travelportPricingAdapter(TravelportClient client) {
        return new TravelportPricingAdapter(client);
    }

    // ---------------------------------------------------------------------
    //  Amadeus
    // ---------------------------------------------------------------------

    @Bean
    @ConditionalOnProperty(prefix = "providers.amadeus", name = "enabled", havingValue = "true")
    public AmadeusClient amadeusClient(AmadeusProperties properties) {
        return new AmadeusClient(properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "providers.amadeus", name = "enabled", havingValue = "true")
    public FlightSearchProvider amadeusFlightSearchAdapter(AmadeusClient client) {
        return new AmadeusFlightSearchAdapter(client);
    }

    @Bean
    @ConditionalOnProperty(prefix = "providers.amadeus", name = "enabled", havingValue = "true")
    public OrderManagementProvider amadeusOrderAdapter(AmadeusClient client) {
        return new AmadeusOrderAdapter(client);
    }

    @Bean
    @ConditionalOnProperty(prefix = "providers.amadeus", name = "enabled", havingValue = "true")
    public PricingProvider amadeusPricingAdapter(AmadeusClient client) {
        return new AmadeusPricingAdapter(client);
    }
}
