package com.coreorder.utilities.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Thin, framework-light logging helpers built on SLF4J.
 * <p>
 * Keeps logging concerns consistent across every module without pulling in
 * Spring or a specific logging implementation at compile time.
 */
public final class LogUtils {

    private LogUtils() {
    }

    /**
     * @return an SLF4J logger bound to the supplied class.
     */
    public static Logger forClass(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * @return an SLF4J logger bound to an explicit logger name.
     */
    public static Logger forName(String name) {
        return LoggerFactory.getLogger(name);
    }

    /**
     * Put a correlation id into the logging MDC so that it is attached to every
     * line emitted during the current thread's processing.
     */
    public static void putCorrelationId(String correlationId) {
        if (correlationId != null && !correlationId.isBlank()) {
            MDC.put("correlationId", correlationId);
        }
    }

    /**
     * Remove the correlation id from the logging MDC for the current thread.
     */
    public static void clearCorrelationId() {
        MDC.remove("correlationId");
    }
}
