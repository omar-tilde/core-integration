package com.core.service.utilities.string;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * Null-safe, dependency-free string helpers shared across the platform.
 */
public final class StringUtils {

    private StringUtils() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    public static String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    /**
     * Safely truncate {@code value} to at most {@code maxLength} characters,
     * appending an ellipsis when truncation occurs. Null-safe.
     */
    public static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (maxLength <= 0) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return maxLength == 1
                ? value.substring(0, 1)
                : value.substring(0, maxLength - 1) + "…";
    }

    /**
     * @return a URL-safe, randomly generated token (no dashes), useful for
     *         correlation ids, nonces or short-lived references.
     */
    public static String randomToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String toBase64(String value) {
        if (value == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
