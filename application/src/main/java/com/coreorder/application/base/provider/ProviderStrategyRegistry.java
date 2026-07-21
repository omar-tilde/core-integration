package com.coreorder.application.base.provider;

import com.coreorder.application.base.exception.NoProviderAvailableException;
import com.coreorder.application.base.exception.ProviderNotFoundException;
import com.coreorder.application.base.exception.ProviderUnavailableException;
import com.coreorder.domain.model.enums.ProviderType;
import com.coreorder.domain.port.out.ProviderStrategy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generic, strongly-typed strategy registry for provider operations.
 * Resolves domain provider strategies and translates domain exceptions into application-level exceptions.
 *
 * @param <T> strategy interface extending {@link ProviderStrategy}
 */
public class ProviderStrategyRegistry<T extends ProviderStrategy> {

    private final Map<ProviderType, T> strategyMap;
    private final String strategyName;

    public ProviderStrategyRegistry(List<T> strategies, String strategyName) {
        this.strategyName = strategyName;
        this.strategyMap = new EnumMap<>(ProviderType.class);
        if (strategies != null) {
            for (T strategy : strategies) {
                if (strategy != null && strategy.providerType() != null) {
                    this.strategyMap.put(strategy.providerType(), strategy);
                }
            }
        }
    }

    /**
     * Get a specific provider strategy by enum type.
     */
    public T getProvider(ProviderType providerType) {
        if (providerType == null) {
            throw new ProviderNotFoundException("No %s specified".formatted(strategyName));
        }
        return Optional.ofNullable(strategyMap.get(providerType))
                .orElseThrow(() -> new ProviderNotFoundException(
                        "No %s found with ID: '%s'".formatted(strategyName, providerType.name())));
    }

    /**
     * Get a specific provider strategy by string identifier code.
     */
    public T getProvider(String providerCode) {
        try {
            ProviderType type = ProviderType.fromCode(providerCode);
            return getProvider(type);
        } catch (IllegalArgumentException e) {
            throw new ProviderNotFoundException(
                    "No %s found with ID: '%s'".formatted(strategyName, providerCode));
        }
    }

    /**
     * Get the first available provider strategy.
     */
    public T getFirstAvailableProvider() {
        return strategyMap.values().stream()
                .filter(ProviderStrategy::isAvailable)
                .findFirst()
                .orElseThrow(() -> new NoProviderAvailableException(
                        "No %s is currently available".formatted(strategyName)));
    }

    /**
     * Resolve provider strategy — prefers the specified provider if given and available,
     * otherwise falls back to the first available provider.
     */
    public T resolveProvider(ProviderType preferredProviderType) {
        if (preferredProviderType != null) {
            T provider = strategyMap.get(preferredProviderType);
            if (provider == null) {
                throw new ProviderNotFoundException(
                        "%s '%s' not found".formatted(capitalize(strategyName), preferredProviderType.name()));
            }
            if (!provider.isAvailable()) {
                throw new ProviderUnavailableException(
                        "%s '%s' is currently unavailable".formatted(capitalize(strategyName), preferredProviderType.name()));
            }
            return provider;
        }
        return getFirstAvailableProvider();
    }

    /**
     * Resolve provider strategy by string code.
     */
    public T resolveProvider(String preferredProviderCode) {
        if (preferredProviderCode != null && !preferredProviderCode.isBlank()) {
            try {
                ProviderType type = ProviderType.fromCode(preferredProviderCode);
                return resolveProvider(type);
            } catch (IllegalArgumentException e) {
                throw new ProviderNotFoundException(
                        "%s '%s' not found".formatted(capitalize(strategyName), preferredProviderCode));
            }
        }
        return getFirstAvailableProvider();
    }

    /**
     * Get all currently available provider enum types.
     */
    public List<ProviderType> getAvailableProviderTypes() {
        return strategyMap.values().stream()
                .filter(ProviderStrategy::isAvailable)
                .map(ProviderStrategy::providerType)
                .toList();
    }

    /**
     * Get all currently available provider IDs as strings.
     */
    public List<String> getAvailableProviderIds() {
        return strategyMap.values().stream()
                .filter(ProviderStrategy::isAvailable)
                .map(ProviderStrategy::providerId)
                .toList();
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
