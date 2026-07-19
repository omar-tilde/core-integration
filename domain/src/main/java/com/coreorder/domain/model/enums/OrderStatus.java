package com.coreorder.domain.model.enums;

/**
 * Lifecycle status of an order.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    TICKETED,
    CANCELLED,
    REFUNDED,
    EXCHANGED;
}
