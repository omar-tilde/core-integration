package com.core.service.presentation.security;

import com.core.service.application.flightsearch.FlightSearchService;
import com.core.service.application.order.OrderService;
import com.core.service.presentation.controller.FlightSearchController;
import com.core.service.presentation.controller.OrderController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Presentation-layer test for API-key authentication.
 * <p>
 * Security is an HTTP concern owned by the presentation layer, so its behaviour is
 * verified here. A {@code @SpringBootTest} (web environment MOCK) is used rather than
 * a {@code @WebMvcTest} slice because the custom {@code SecurityFilterChain} depends on
 * the {@code HttpSecurity} bean, which Spring Boot's web-MVC test slice does not
 * auto-configure. The presentation package is component-scanned so the REST
 * controllers and the security filter chain are wired together, while the downstream
 * application services are mocked.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(ApiKeySecurityTest.PresentationTestConfig.class)
@TestPropertySource(properties = "api.key=test-secret-key")
class ApiKeySecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FlightSearchService flightSearchService;

    @MockitoBean
    private OrderService orderService;

    @Test
    void publicErrorEndpointIsAccessibleWithoutApiKey() throws Exception {
        // /error is on the public allow-list, so the security filter must NOT reject it
        // with 401. A direct hit has no prior error, so the error controller answers 5xx
        // — the assertion that matters is that authentication did not block the request.
        mockMvc.perform(get("/error"))
                .andExpect(status().is5xxServerError());
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

    /**
     * Scans the presentation package (controllers + the security filter chain) and
     * registers {@link ApiKeyProperties} (bound from {@code api.key}).
     */
    @Configuration(proxyBeanMethods = false)
    @ComponentScan(basePackages = "com.core.service.presentation")
    @EnableConfigurationProperties(ApiKeyProperties.class)
    static class PresentationTestConfig {
    }
}
