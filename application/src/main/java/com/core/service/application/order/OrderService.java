package com.core.service.application.order;

import com.core.service.application.base.exception.InvalidCommandException;
import com.core.service.application.base.exception.ProviderCommunicationException;
import com.core.service.application.base.provider.ProviderRouter;
import com.core.service.domain.exception.DomainException;
import com.core.service.domain.model.entity.Order;
import com.core.service.domain.model.entity.Passenger;
import com.core.service.domain.model.valueobject.Money;
import com.core.service.domain.model.valueobject.PassengerType;
import com.core.service.domain.port.out.OrderManagementProvider;
import com.core.service.domain.port.out.OrderManagementProvider.OrderCreateRequest;
import com.core.service.domain.port.out.OrderManagementProvider.PaymentInfo;
import com.core.service.domain.port.out.OrderManagementProvider.PaymentMethod;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Application service for order management operations.
 * Maps commands to domain requests and converts domain exceptions to application exceptions.
 */
@Service
public class OrderService {

    private final ProviderRouter providerRouter;

    public OrderService(ProviderRouter providerRouter) {
        this.providerRouter = providerRouter;
    }

    /**
     * Create a new order through the specified (or default) provider.
     */
    public OrderDto createOrder(CreateOrderCommand command) {
        OrderManagementProvider provider = providerRouter.resolveOrderManagementProvider(command.providerId());
        OrderCreateRequest request = toOrderCreateRequest(command);

        try {
            Order order = provider.createOrder(request);
            return OrderMapper.toDto(order);
        } catch (DomainException e) {
            throw new ProviderCommunicationException(e.getMessage(), e);
        }
    }

    /**
     * Retrieve an order by its ID.
     */
    public OrderDto retrieveOrder(RetrieveOrderQuery query) {
        OrderManagementProvider provider = providerRouter.getOrderManagementProvider(query.providerId());

        try {
            Order order = provider.retrieveOrder(query.orderId());
            return OrderMapper.toDto(order);
        } catch (DomainException e) {
            throw new ProviderCommunicationException(e.getMessage(), e);
        }
    }

    /**
     * Cancel an existing order.
     */
    public OrderDto cancelOrder(String orderId, String providerId) {
        OrderManagementProvider provider = providerRouter.getOrderManagementProvider(providerId);

        try {
            Order order = provider.cancelOrder(orderId);
            return OrderMapper.toDto(order);
        } catch (DomainException e) {
            throw new ProviderCommunicationException(e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw new InvalidCommandException(e.getMessage());
        }
    }

    /**
     * Get available order management provider IDs.
     */
    public List<String> getAvailableProviders() {
        return providerRouter.getAvailableOrderManagementProviderIds();
    }

    private OrderCreateRequest toOrderCreateRequest(CreateOrderCommand command) {
        try {
            List<Passenger> passengers = command.passengers().stream()
                    .map(this::toPassenger)
                    .toList();

            PaymentInfo paymentInfo = new PaymentInfo(
                    PaymentMethod.valueOf(command.payment().method().toUpperCase()),
                    command.payment().cardNumber(),
                    command.payment().cardHolderName(),
                    command.payment().expiryMonth(),
                    command.payment().expiryYear(),
                    command.payment().cvv()
            );

            return new OrderCreateRequest(
                    command.offerId(),
                    passengers,
                    Money.zero("EUR"),
                    paymentInfo
            );
        } catch (IllegalArgumentException e) {
            throw new InvalidCommandException("Invalid order create parameters: " + e.getMessage());
        }
    }

    private Passenger toPassenger(CreateOrderCommand.PassengerData data) {
        Passenger.DocumentInfo documentInfo = null;
        if (data.document() != null) {
            documentInfo = new Passenger.DocumentInfo(
                    Passenger.DocumentType.valueOf(data.document().type().toUpperCase()),
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
                PassengerType.valueOf(data.type().toUpperCase()),
                documentInfo
        );
    }
}
