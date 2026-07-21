package com.coreorder.domain.port.out;

import com.coreorder.domain.model.entity.Order;
import com.coreorder.domain.model.entity.Passenger;
import com.coreorder.domain.model.valueobject.Money;

import java.util.List;

/**
 * Outbound port for order management providers (Travelport, Amadeus, Sabre, etc.).
 */
public interface OrderManagementProvider extends ProviderStrategy {

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
     * Provider-agnostic order creation request.
     */
    record OrderCreateRequest(
            String offerId,
            List<Passenger> passengers,
            Money expectedTotalPrice,
            PaymentInfo payment
    ) {}

    /**
     * Payment information captured at booking time.
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
