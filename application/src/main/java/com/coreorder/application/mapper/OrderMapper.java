package com.coreorder.application.mapper;

import java.util.List;

import com.coreorder.application.dto.FlightOfferDto;
import com.coreorder.application.dto.OrderDto;
import com.coreorder.domain.model.entity.Order;

/**
 * Maps domain Order entities to DTOs.
 */
public final class OrderMapper {

    private OrderMapper() {}

    public static OrderDto toDto(Order order) {
        String providerId = order.providerId();
        return new OrderDto(
                order.orderId(),
                order.providerOrderId(),
                providerId,
                order.passengers().stream()
                        .map(p -> new OrderDto.PassengerDto(
                                p.passengerId(),
                                p.firstName(),
                                p.lastName(),
                                p.dateOfBirth(),
                                p.type().name()
                        ))
                        .toList(),
                FlightOfferMapper.toDtoList(order.offers(), providerId),
                new FlightOfferDto.PriceDto(
                        order.totalPrice().amount(),
                        order.totalPrice().currency().getCurrencyCode()
                ),
                order.status().name(),
                order.createdAt(),
                order.updatedAt(),
                order.ticketedAt()
        );
    }

    public static List<OrderDto> toDtoList(List<Order> orders) {
        return orders.stream()
                .map(OrderMapper::toDto)
                .toList();
    }
}
