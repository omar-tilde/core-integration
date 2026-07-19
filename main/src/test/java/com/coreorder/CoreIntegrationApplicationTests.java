package com.coreorder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test that verifies the full Spring context boots cleanly.
 * <p>
 * Skipped in environments where external provider credentials are unavailable
 * (we do not block CI on outbound provider calls).
 */
@SpringBootTest
class CoreIntegrationApplicationTests {

    @Test
    void contextLoads() {
        // intentionally empty — the @SpringBootTest annotation asserts the
        // ApplicationContext loads without errors.
    }
}
