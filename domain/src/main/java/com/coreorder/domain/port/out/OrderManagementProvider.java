package com.coreorder.domain.port.out;

import java.util.List;

import com.coreorder.domain.model.entity.Order;
import com.coreorder.domain.model.entity.Passenger;
import com.coreorder.domain.model.valueobject.Money;

/**
 * Outbound port for order management providers (Travelport, Amadeus, Sabre, etc.).
 * <p>
 * Encapsulates create / retrieve / cancel operations on orders. Provider-specific request
 * payloads are kept inside {@link OrderCreateRequest} to avoid leaking transport details
 * to higher layers.
 */
public interface OrderManagementProvider {

    /**
     * @return stable identifier for this provider, e.g. "AMADEUS" or "TRAVELPORT".
     */
    String providerId();

    /**
     * Create a new order on the provider system.
     *
     * @param request provider-agnostic order request
     * @return created {@link Order} including the provider-assigned order id
     */
    Order createOrder(OrderCreateRequest request);

    /**
     * Retrieve an existing order by its provider-side id.
     *
     * @param providerOrderId id assigned by the provider
     * @return the {@link Order} as it currently exists on the provider system
     */
    Order retrieveOrder(String providerOrderId);

    /**
     * Cancel an order on the provider system.
     *
     * @param providerOrderId id assigned by the provider
     * @return the cancelled {@link Order} reflecting the new state
     */
    Order cancelOrder(String providerOrderId);

    /**
     * @return whether this provider is currently reachable and enabled.
     */
    boolean isAvailable();

    /**
     * Provider-agnostic order creation request.
     *
     * @param offerId            identifier of the priced offer being booked
     * @param passengers         passengers travelling under the order
     * @param expectedTotalPrice the price the caller expects to be charged (the provider
     *                           may re-validate / re-quote; the response reflects the
     *                           final accepted price)
     * @param payment            payment information (card, billing, etc.)
     */
    record OrderCreateRequest(
            String offerId,
            List<Passenger> passengers,
            Money expectedTotalPrice,
            PaymentInfo payment
    ) {}

    /**
     * Payment information captured at booking time.
     *
     * @param method         payment method (CARD, VOUCHER, ...)
     * @param cardNumber     PAN or equivalent token (never logged)
     * @param cardHolderName name as it appears on the card
     * @param expiryMonth    two-digit month string ("07")
     * @param expiryYear     two- or four-digit year string ("2027")
     * @param cvv            card verification value (never logged)
     */
    record PaymentInfo(
            PaymentMethod method,
            String cardNumber,
            String cardHolderName,
            String expiryMonth,
            String expiryYear,
            String cvv
    ) {}

    /**
     * Supported payment methods at the order level.
     */
    enum PaymentMethod {
        CARD,
        VOUCHER,
        INVOICE,
        WALLET,
        OTHER
    }
}
