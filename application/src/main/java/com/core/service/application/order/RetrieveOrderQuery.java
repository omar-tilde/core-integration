package com.core.service.application.order;

import com.core.service.application.base.exception.InvalidCommandException;

/**
 * Query input object for retrieving an order by ID.
 */
public record RetrieveOrderQuery(
        String orderId,
        String providerId
) {
    public RetrieveOrderQuery {
        if (orderId == null || orderId.isBlank()) {
            throw new InvalidCommandException("Order ID must not be null or blank");
        }
    }
}
