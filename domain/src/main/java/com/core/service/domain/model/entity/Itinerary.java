package com.core.service.domain.model.entity;

import com.core.service.domain.model.valueobject.AirportCode;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * An itinerary is an ordered sequence of flight segments forming a single direction of travel.
 * For a one-way trip, there is one itinerary; for a round-trip, there are two.
 * Immutable domain entity.
 */
public record Itinerary(List<Segment> segments) {

    public Itinerary {
        Objects.requireNonNull(segments, "Segments must not be null");
        if (segments.isEmpty()) {
            throw new IllegalArgumentException("Itinerary must contain at least one segment");
        }
        segments = List.copyOf(segments);

        // Validate segment continuity
        for (int i = 1; i < segments.size(); i++) {
            Segment previous = segments.get(i - 1);
            Segment current = segments.get(i);
            if (!previous.destination().equals(current.origin())) {
                throw new IllegalArgumentException(
                        "Segment discontinuity: segment %d arrives at %s but segment %d departs from %s"
                                .formatted(i - 1, previous.destination(), i, current.origin()));
            }
            if (!current.departureTime().isAfter(previous.arrivalTime())) {
                throw new IllegalArgumentException(
                        "Segment %d departs before segment %d arrives".formatted(i, i - 1));
            }
        }
    }

    public AirportCode origin() {
        return segments.getFirst().origin();
    }

    public AirportCode destination() {
        return segments.getLast().destination();
    }

    public Duration totalDuration() {
        return Duration.between(
                segments.getFirst().departureTime(),
                segments.getLast().arrivalTime()
        );
    }

    public Duration totalLayoverDuration() {
        Duration flightTime = Duration.ZERO;
        for (Segment segment : segments) {
            flightTime = flightTime.plus(segment.duration());
        }
        return totalDuration().minus(flightTime);
    }

    public int numberOfStops() {
        return segments.size() - 1;
    }

    public boolean isDirect() {
        return segments.size() == 1;
    }

    public boolean isConnecting() {
        return segments.size() > 1;
    }
}
