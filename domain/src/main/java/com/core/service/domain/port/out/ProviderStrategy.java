package com.core.service.domain.port.out;

import com.core.service.domain.model.enums.ProviderType;

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
     * @return whether this provider adapter is currently enabled and may be used.
     */
    boolean isEnabled();
}
