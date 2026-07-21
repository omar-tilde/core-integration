package com.coreorder.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that API-key authentication protects application endpoints while leaving the
 * explicitly public allow-list (actuator health/info) open. The security filter chain is
 * applied to {@link MockMvc} via {@link AutoConfigureMockMvc}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "api.key=test-secret-key")
class ApiKeySecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicActuatorHealthIsAccessibleWithoutApiKey() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointRejectsRequestWithoutApiKey() throws Exception {
        mockMvc.perform(get("/api/v1/flights/providers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointRejectsRequestWithInvalidApiKey() throws Exception {
        mockMvc.perform(get("/api/v1/flights/providers")
                        .header("X-API-KEY", "wrong-key"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointAllowsRequestWithValidApiKey() throws Exception {
        mockMvc.perform(get("/api/v1/flights/providers")
                        .header("X-API-KEY", "test-secret-key"))
                .andExpect(status().isOk());
    }
}
