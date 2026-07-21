package com.coreorder.domain.port.out;

import com.coreorder.domain.model.enums.ProviderType;

/**
 * Base contract for outbound provider strategy interfaces.
 */
public interface ProviderStrategy {

    /**
     * @return strongly typed provider classification enum.
     */
    ProviderType providerType();

    /**
     * @return stable string identifier for backward compatibility and string serialization.
     */
    default String providerId() {
        return providerType() != null ? providerType().name() : null;
    }

    /**
     * @return whether this provider adapter is currently reachable and enabled.
     */
    boolean isAvailable();
}
