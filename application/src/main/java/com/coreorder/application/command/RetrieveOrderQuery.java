package com.coreorder.application.command;

/**
 * Query for retrieving an order by its ID and optionally a specific provider.
 */
public record RetrieveOrderQuery(
        String orderId,
        String providerId
) {

    public RetrieveOrderQuery {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID must not be blank");
        }
    }
}
