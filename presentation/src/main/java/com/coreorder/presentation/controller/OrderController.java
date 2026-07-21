package com.coreorder.presentation.controller;

import com.coreorder.application.order.CreateOrderCommand;
import com.coreorder.application.order.OrderDto;
import com.coreorder.application.order.OrderService;
import com.coreorder.application.order.RetrieveOrderQuery;
import com.coreorder.presentation.request.CreateOrderRequest;
import com.coreorder.presentation.response.OrderResponse;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for order management operations.
 * Exposes API endpoints using application commands and services.
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Create a new order.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        CreateOrderCommand command = mapToCommand(request);
        OrderDto order = orderService.createOrder(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new OrderResponse(order));
    }

    /**
     * Retrieve an existing order.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> retrieveOrder(
            @PathVariable String orderId,
            @RequestParam String providerId
    ) {
        var query = new RetrieveOrderQuery(orderId, providerId);
        OrderDto order = orderService.retrieveOrder(query);

        return ResponseEntity.ok(new OrderResponse(order));
    }

    /**
     * Cancel an existing order.
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable String orderId,
            @RequestParam String providerId
    ) {
        OrderDto order = orderService.cancelOrder(orderId, providerId);

        return ResponseEntity.ok(new OrderResponse(order));
    }

    /**
     * Get available order management providers.
     */
    @GetMapping("/providers")
    public ResponseEntity<List<String>> getAvailableProviders() {
        return ResponseEntity.ok(orderService.getAvailableProviders());
    }

    private CreateOrderCommand mapToCommand(CreateOrderRequest request) {
        List<CreateOrderCommand.PassengerData> passengers = request.passengers().stream()
                .map(p -> new CreateOrderCommand.PassengerData(
                        p.firstName(),
                        p.lastName(),
                        LocalDate.parse(p.dateOfBirth()),
                        p.type(),
                        p.document() != null ? new CreateOrderCommand.PassengerData.DocumentData(
                                p.document().type(),
                                p.document().number(),
                                p.document().issuingCountry(),
                                LocalDate.parse(p.document().expirationDate())
                        ) : null
                ))
                .toList();

        var payment = new CreateOrderCommand.PaymentData(
                request.payment().method(),
                request.payment().cardNumber(),
                request.payment().cardHolderName(),
                request.payment().expiryMonth(),
                request.payment().expiryYear(),
                request.payment().cvv()
        );

        return new CreateOrderCommand(
                request.offerId(),
                request.providerId(),
                passengers,
                payment
        );
    }
}
