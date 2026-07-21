package com.core.service.application.base.provider;

import com.core.service.application.base.exception.ProviderNotFoundException;
import com.core.service.application.base.exception.ProviderUnavailableException;
import com.core.service.domain.model.enums.ProviderType;
import com.core.service.domain.port.out.ProviderStrategy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Generic, strongly-typed strategy registry for provider operations.
 * Resolves domain provider strategies and translates domain exceptions into application-level exceptions.
 * <p>
 * A provider is only ever used when it is explicitly requested. Fallback to another provider is
 * intentionally absent: if the requested provider does not exist, is disabled, or cannot be resolved,
 * the request fails immediately with a clear error.
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
     * Resolve the explicitly requested provider by enum type.
     * Fails immediately (no fallback) when the provider is missing or disabled.
     */
    public T getProvider(ProviderType providerType) {
        if (providerType == null) {
            throw new ProviderNotFoundException(
                    "A provider must be explicitly specified for the %s.".formatted(strategyName));
        }
        T provider = strategyMap.get(providerType);
        if (provider == null) {
            throw new ProviderNotFoundException(
                    "Requested %s '%s' is not available.".formatted(strategyName, providerType.name()));
        }
        if (!provider.isEnabled()) {
            throw new ProviderUnavailableException(
                    "Requested %s '%s' is disabled.".formatted(strategyName, providerType.name()));
        }
        return provider;
    }

    /**
     * Resolve the explicitly requested provider by its string identifier.
     * Fails immediately (no fallback) when the provider is missing, cannot be resolved, or is disabled.
     */
    public T getProvider(String providerCode) {
        if (providerCode == null || providerCode.isBlank()) {
            throw new ProviderNotFoundException(
                    "A provider must be explicitly specified for the %s.".formatted(strategyName));
        }
        try {
            ProviderType type = ProviderType.fromCode(providerCode);
            if (type == null) {
                throw new ProviderNotFoundException(
                        "Requested %s '%s' is not available.".formatted(strategyName, providerCode));
            }
            return getProvider(type);
        } catch (IllegalArgumentException e) {
            throw new ProviderNotFoundException(
                    "Requested %s '%s' is not available.".formatted(strategyName, providerCode));
        }
    }

    /**
     * Resolve the explicitly requested provider by enum type.
     * This is an explicit-request operation: it never falls back to another provider.
     */
    public T resolveProvider(ProviderType preferredProviderType) {
        return getProvider(preferredProviderType);
    }

    /**
     * Resolve the explicitly requested provider by its string identifier.
     * This is an explicit-request operation: it never falls back to another provider.
     */
    public T resolveProvider(String preferredProviderCode) {
        return getProvider(preferredProviderCode);
    }

    /**
     * Get all currently enabled provider enum types.
     */
    public List<ProviderType> getAvailableProviderTypes() {
        return strategyMap.values().stream()
                .filter(ProviderStrategy::isEnabled)
                .map(ProviderStrategy::providerType)
                .toList();
    }

    /**
     * Get all currently enabled provider IDs as strings.
     */
    public List<String> getAvailableProviderIds() {
        return strategyMap.values().stream()
                .filter(ProviderStrategy::isEnabled)
                .map(ProviderStrategy::providerId)
                .toList();
    }
}
