package com.core.service.utilities.date;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Immutable, dependency-free date/time helpers built on {@code java.time}.
 */
public final class DateUtils {

    private DateUtils() {
    }

    public static Instant nowUtc() {
        return Instant.now().truncatedTo(ChronoUnit.MILLIS);
    }

    public static LocalDate todayUtc() {
        return LocalDate.now(ZoneOffset.UTC);
    }

    public static String formatIso(Instant instant) {
        return instant == null ? null : instant.toString();
    }

    public static Instant parseIso(String value) {
        return value == null || value.isBlank() ? null : Instant.parse(value);
    }

    public static String formatDate(LocalDate date, String pattern) {
        if (date == null) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDate parseDate(String value, String pattern) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern));
    }
}
