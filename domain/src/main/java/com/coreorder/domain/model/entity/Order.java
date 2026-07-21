package com.coreorder.domain.model.entity;

import com.coreorder.domain.model.enums.OrderStatus;
import com.coreorder.domain.model.valueobject.Money;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * An order represents a confirmed booking made through the system.
 * Contains passengers, flight offers, pricing, and lifecycle status.
 * <p>
 * This is an aggregate root in DDD terms — all modifications to its contents
 * go through the Order entity itself.
 */
public class Order {

    private final String orderId;
    private final String providerOrderId;
    private final String providerId;
    private final List<Passenger> passengers;
    private final List<FlightOffer> offers;
    private final Money totalPrice;
    private OrderStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant ticketedAt;

    public Order(
            String orderId,
            String providerOrderId,
            String providerId,
            List<Passenger> passengers,
            List<FlightOffer> offers,
            Money totalPrice,
            OrderStatus status,
            Instant createdAt
    ) {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(providerOrderId, "Provider order ID must not be null");
        Objects.requireNonNull(providerId, "Provider ID must not be null");
        Objects.requireNonNull(passengers, "Passengers must not be null");
        Objects.requireNonNull(offers, "Offers must not be null");
        Objects.requireNonNull(totalPrice, "Total price must not be null");
        Objects.requireNonNull(status, "Status must not be null");
        Objects.requireNonNull(createdAt, "Created at must not be null");

        if (passengers.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one passenger");
        }
        if (offers.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one offer");
        }

        this.orderId = orderId;
        this.providerOrderId = providerOrderId;
        this.providerId = providerId;
        this.passengers = List.copyOf(passengers);
        this.offers = List.copyOf(offers);
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    // --- Accessors ---

    public String orderId() {
        return orderId;
    }

    public String providerOrderId() {
        return providerOrderId;
    }

    public String providerId() {
        return providerId;
    }

    public List<Passenger> passengers() {
        return passengers;
    }

    public List<FlightOffer> offers() {
        return offers;
    }

    public Money totalPrice() {
        return totalPrice;
    }

    public OrderStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public Instant ticketedAt() {
        return ticketedAt;
    }

    // --- Domain operations ---

    public void confirm() {
        ensureStatus(OrderStatus.PENDING, "Only pending orders can be confirmed");
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    public void ticket() {
        ensureStatus(OrderStatus.CONFIRMED, "Only confirmed orders can be ticketed");
        this.status = OrderStatus.TICKETED;
        this.ticketedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }
        if (status == OrderStatus.TICKETED) {
            throw new IllegalStateException("Cannot cancel a ticketed order; use refund instead");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public void refund() {
        ensureStatus(OrderStatus.TICKETED, "Only ticketed orders can be refunded");
        this.status = OrderStatus.REFUNDED;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED || status == OrderStatus.TICKETED;
    }

    public int passengerCount() {
        return passengers.size();
    }

    private void ensureStatus(OrderStatus expected, String errorMessage) {
        if (this.status != expected) {
            throw new IllegalStateException(errorMessage + " (current status: " + this.status + ")");
        }
    }
}
