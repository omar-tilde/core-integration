package com.core.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Core Integration — Aviation services aggregator.
 * <p>
 * Main entry point for the Spring Boot application. Wires all layers together:
 * domain → application → infrastructure → presentation.
 */
@SpringBootApplication
@ConfigurationPropertiesScan("com.core.service")
public class CoreIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreIntegrationApplication.class, args);
    }
}
