package com.core.service.application.flightsearch;

import com.core.service.domain.model.entity.FlightOffer;
import com.core.service.domain.model.entity.Itinerary;
import com.core.service.domain.model.entity.Segment;

import java.util.List;

/**
 * Stateless mapper between domain {@link FlightOffer} entities and application {@link FlightOfferDto} DTOs.
 */
public final class FlightOfferMapper {

    private FlightOfferMapper() {}

    public static FlightOfferDto toDto(FlightOffer offer, String providerId) {
        if (offer == null) {
            return null;
        }

        var itineraryDtos = offer.itineraries().stream()
                .map(FlightOfferMapper::toItineraryDto)
                .toList();

        var totalPriceDto = new FlightOfferDto.PriceDto(
                offer.totalPrice().amount(),
                offer.totalPrice().currency().getCurrencyCode()
        );

        var basePriceDto = new FlightOfferDto.PriceDto(
                offer.basePrice().amount(),
                offer.basePrice().currency().getCurrencyCode()
        );

        var taxAmountDto = new FlightOfferDto.PriceDto(
                offer.taxAmount().amount(),
                offer.taxAmount().currency().getCurrencyCode()
        );

        return new FlightOfferDto(
                offer.offerId(),
                providerId,
                itineraryDtos,
                totalPriceDto,
                basePriceDto,
                taxAmountDto,
                offer.validatingCarrier(),
                offer.refundable(),
                offer.seatsAvailable()
        );
    }

    public static List<FlightOfferDto> toDtoList(List<FlightOffer> offers, String providerId) {
        if (offers == null) {
            return List.of();
        }
        return offers.stream()
                .map(offer -> toDto(offer, providerId))
                .toList();
    }

    private static FlightOfferDto.ItineraryDto toItineraryDto(Itinerary itinerary) {
        var segmentDtos = itinerary.segments().stream()
                .map(FlightOfferMapper::toSegmentDto)
                .toList();

        return new FlightOfferDto.ItineraryDto(
                itinerary.origin().code(),
                itinerary.destination().code(),
                itinerary.segments().getFirst().departureTime(),
                itinerary.segments().getLast().arrivalTime(),
                itinerary.totalDuration(),
                itinerary.numberOfStops(),
                segmentDtos
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
}
