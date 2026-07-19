package com.coreorder.application.service;

import java.util.List;
import java.util.UUID;

import com.coreorder.application.command.CreateOrderCommand;
import com.coreorder.application.command.RetrieveOrderQuery;
import com.coreorder.application.dto.OrderDto;
import com.coreorder.application.mapper.OrderMapper;
import com.coreorder.application.provider.ProviderRouter;
import com.coreorder.domain.model.entity.Order;
import com.coreorder.domain.model.entity.Passenger;
import com.coreorder.domain.model.valueobject.Money;
import com.coreorder.domain.port.out.OrderManagementProvider;
import com.coreorder.domain.port.out.OrderManagementProvider.OrderCreateRequest;
import com.coreorder.domain.port.out.OrderManagementProvider.PaymentInfo;
import com.coreorder.domain.port.out.OrderManagementProvider.PaymentMethod;

/**
 * Application service for order management operations.
 * Orchestrates order creation, retrieval, and cancellation.
 */
public class OrderService {

    private final ProviderRouter providerRouter;

    public OrderService(ProviderRouter providerRouter) {
        this.providerRouter = providerRouter;
    }

    /**
     * Create a new order through the specified (or default) provider.
     *
     * @param command the order creation command
     * @return the created order as a DTO
     */
    public OrderDto createOrder(CreateOrderCommand command) {
        // 1. Resolve provider
        OrderManagementProvider provider = providerRouter.resolveOrderManagementProvider(command.providerId());

        // 2. Map command to domain request
        OrderCreateRequest request = toOrderCreateRequest(command);

        // 3. Create order through provider
        Order order = provider.createOrder(request);

        // 4. Map to DTO
        return OrderMapper.toDto(order);
    }

    /**
     * Retrieve an order by its ID.
     *
     * @param query the retrieval query
     * @return the order as a DTO
     */
    public OrderDto retrieveOrder(RetrieveOrderQuery query) {
        // If a provider is specified, use it directly; otherwise try the first available
        OrderManagementProvider provider;
        if (query.providerId() != null) {
            provider = providerRouter.getOrderManagementProvider(query.providerId());
        } else {
            provider = providerRouter.getFirstAvailableOrderManagementProvider();
        }

        Order order = provider.retrieveOrder(query.orderId());
        return OrderMapper.toDto(order);
    }

    /**
     * Cancel an existing order.
     *
     * @param orderId    the order ID to cancel
     * @param providerId the provider that owns the order
     * @return the cancelled order as a DTO
     */
    public OrderDto cancelOrder(String orderId, String providerId) {
        OrderManagementProvider provider = providerRouter.getOrderManagementProvider(providerId);
        Order order = provider.cancelOrder(orderId);
        return OrderMapper.toDto(order);
    }

    /**
     * Get available order management provider IDs.
     */
    public List<String> getAvailableProviders() {
        return providerRouter.getAvailableOrderManagementProviderIds();
    }

    private OrderCreateRequest toOrderCreateRequest(CreateOrderCommand command) {
        List<Passenger> passengers = command.passengers().stream()
                .map(this::toPassenger)
                .toList();

        PaymentInfo paymentInfo = new PaymentInfo(
                PaymentMethod.valueOf(command.payment().method()),
                command.payment().cardNumber(),
                command.payment().cardHolderName(),
                command.payment().expiryMonth(),
                command.payment().expiryYear(),
                command.payment().cvv()
        );

        return new OrderCreateRequest(
                command.offerId(),
                passengers,
                Money.zero("EUR"), // placeholder; pricing is confirmed by provider
                paymentInfo
        );
    }

    private Passenger toPassenger(CreateOrderCommand.PassengerData data) {
        Passenger.DocumentInfo documentInfo = null;
        if (data.document() != null) {
            documentInfo = new Passenger.DocumentInfo(
                    Passenger.DocumentType.valueOf(data.document().type()),
                    data.document().number(),
                    data.document().issuingCountry(),
                    data.document().expirationDate()
            );
        }

        return new Passenger(
                UUID.randomUUID().toString(),
                data.firstName(),
                data.lastName(),
                data.dateOfBirth(),
                data.type(),
                documentInfo
        );
    }
}
