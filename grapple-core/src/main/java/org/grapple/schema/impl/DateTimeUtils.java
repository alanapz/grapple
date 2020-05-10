package org.grapple.schema.impl;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

    private static final ZoneId UTC = ZoneId.of("UTC");

    private DateTimeUtils() {

    }

    public static String formatDateTimeLocal(Instant instant, ZoneId zoneId, String pattern) {
        requireNonNull(zoneId, "zoneId");
        if (instant == null) {
            return null;
        }
        final LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zoneId);
        if (pattern == null || pattern.trim().isEmpty()) {
            return localDateTime.toString();
        }
        if ("iso".equals(pattern)) {
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime);
        }
        if ("rfc1123".equals(pattern)) {
            return DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(instant, zoneId));
        }
        return DateTimeFormatter.ofPattern(pattern).format(localDateTime);
    }

    public static String formatDateTimeUtc(Instant instant, String pattern) {
        if (instant == null) {
            return null;
        }
        if (pattern == null || pattern.trim().isEmpty()) {
            return instant.toString();
        }
        if ("iso".equals(pattern)) {
            return DateTimeFormatter.ISO_INSTANT.format(instant);
        }
        if ("rfc1123".equals(pattern)) {
            return DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(instant, UTC));
        }
        return DateTimeFormatter.ofPattern(pattern).withZone(UTC).format(instant);
    }
}
