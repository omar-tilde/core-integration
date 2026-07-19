package com.coreorder.application.mapper;

import java.util.List;

import com.coreorder.application.dto.FlightOfferDto;
import com.coreorder.domain.model.entity.FlightOffer;
import com.coreorder.domain.model.entity.Itinerary;
import com.coreorder.domain.model.entity.Segment;
import com.coreorder.domain.model.valueobject.Money;

/**
 * Maps domain FlightOffer entities to DTOs and vice versa.
 * Stateless — all mapping is done via static methods.
 */
public final class FlightOfferMapper {

    private FlightOfferMapper() {}

    public static FlightOfferDto toDto(FlightOffer offer, String providerId) {
        return new FlightOfferDto(
                offer.offerId(),
                providerId,
                offer.itineraries().stream()
                        .map(FlightOfferMapper::toItineraryDto)
                        .toList(),
                toPriceDto(offer.totalPrice()),
                toPriceDto(offer.basePrice()),
                toPriceDto(offer.taxAmount()),
                offer.validatingCarrier(),
                offer.refundable(),
                offer.seatsAvailable()
        );
    }

    public static List<FlightOfferDto> toDtoList(List<FlightOffer> offers, String providerId) {
        return offers.stream()
                .map(offer -> toDto(offer, providerId))
                .toList();
    }

    private static FlightOfferDto.ItineraryDto toItineraryDto(Itinerary itinerary) {
        return new FlightOfferDto.ItineraryDto(
                itinerary.origin().code(),
                itinerary.destination().code(),
                itinerary.segments().getFirst().departureTime(),
                itinerary.segments().getLast().arrivalTime(),
                itinerary.totalDuration(),
                itinerary.numberOfStops(),
                itinerary.segments().stream()
                        .map(FlightOfferMapper::toSegmentDto)
                        .toList()
        );
    }

    private static FlightOfferDto.SegmentDto toSegmentDto(Segment segment) {
        return new FlightOfferDto.SegmentDto(
                segment.flightNumber().toMarketingString(),
                segment.origin().code(),
                segment.destination().code(),
                segment.departureTime(),
                segment.arrivalTime(),
                segment.duration(),
                segment.cabinClass().name(),
                segment.bookingClass().code(),
                segment.availableSeats(),
                segment.aircraftInfo().equipmentCode(),
                segment.aircraftInfo().aircraftType()
        );
    }

    private static FlightOfferDto.PriceDto toPriceDto(Money money) {
        return new FlightOfferDto.PriceDto(money.amount(), money.currency().getCurrencyCode());
    }
}
