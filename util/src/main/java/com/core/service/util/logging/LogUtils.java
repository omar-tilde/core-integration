package com.core.service.util.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

/**
 * Thin, framework-light logging helpers built on Apache Log4j2.
 * <p>
 * Keeps logging consistent across every module without pulling in Spring. The
 * correlation id is stored in Log4j2's {@link ThreadContext} (its MDC equivalent)
 * so it is attached to every line emitted during the current thread's processing.
 */
public final class LogUtils {

    private LogUtils() {
    }

    /**
     * @return a Log4j2 logger bound to the supplied class.
     */
    public static Logger forClass(Class<?> clazz) {
        return LogManager.getLogger(clazz);
    }

    /**
     * @return a Log4j2 logger bound to an explicit logger name.
     */
    public static Logger forName(String name) {
        return LogManager.getLogger(name);
    }

    /**
     * Put a correlation id into the Log4j2 {@link ThreadContext} so that it is
     * attached to every line emitted during the current thread's processing.
     */
    public static void putCorrelationId(String correlationId) {
        if (correlationId != null && !correlationId.isBlank()) {
            ThreadContext.put("correlationId", correlationId);
        }
    }

    /**
     * Remove the correlation id from the Log4j2 {@link ThreadContext} for the
     * current thread.
     */
    public static void clearCorrelationId() {
        ThreadContext.remove("correlationId");
    }
}
