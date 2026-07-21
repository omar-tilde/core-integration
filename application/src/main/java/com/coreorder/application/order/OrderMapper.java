package com.coreorder.application.order;

import com.coreorder.application.flightsearch.FlightOfferDto;
import com.coreorder.application.flightsearch.FlightOfferMapper;
import com.coreorder.domain.model.entity.Order;
import com.coreorder.domain.model.entity.Passenger;

/**
 * Stateless mapper between domain {@link Order} entities and application {@link OrderDto} DTOs.
 */
public final class OrderMapper {

    private OrderMapper() {}

    public static OrderDto toDto(Order order) {
        if (order == null) {
            return null;
        }

        var passengerDtos = order.passengers().stream()
                .map(OrderMapper::toPassengerDto)
                .toList();

        var offerDtos = order.offers().stream()
                .map(offer -> FlightOfferMapper.toDto(offer, order.providerId()))
                .toList();

        var totalPriceDto = new FlightOfferDto.PriceDto(
                order.totalPrice().amount(),
                order.totalPrice().currency().getCurrencyCode()
        );

        return new OrderDto(
                order.orderId(),
                order.providerOrderId(),
                order.providerId(),
                order.status().name(),
                totalPriceDto,
                passengerDtos,
                offerDtos,
                order.createdAt()
        );
    }

    private static OrderDto.PassengerDto toPassengerDto(Passenger passenger) {
        OrderDto.DocumentDto documentDto = null;
        if (passenger.documentInfo() != null) {
            documentDto = new OrderDto.DocumentDto(
                    passenger.documentInfo().type().name(),
                    passenger.documentInfo().number(),
                    passenger.documentInfo().issuingCountry(),
                    passenger.documentInfo().expirationDate()
            );
        }

        return new OrderDto.PassengerDto(
                passenger.passengerId(),
                passenger.firstName(),
                passenger.lastName(),
                passenger.dateOfBirth(),
                passenger.type().name(),
                documentDto
        );
    }
}
