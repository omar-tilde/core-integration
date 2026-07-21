# Core Integration — Architecture Document

> This document is derived directly from the source code. It is the single
> source of truth for how the application is organised, how its modules depend
> on one another, and what each public concept means. If the code and this
> document disagree, the code wins — and this document is updated to match.

---

## 1. What is Core Integration?

**Core Integration** is a **provider-agnostic aviation order & reservation
aggregator**. It exposes a single, unified REST API on top of multiple
distribution back-ends (GDS / NDC) so that downstream consumers never have to
deal with the quirks of any particular provider.

Two providers are wired today:

| Provider    | `ProviderType` | Status    |
|-------------|----------------|-----------|
| Amadeus     | `AMADEUS`      | Enabled   |
| Travelport  | `TRAVELPORT`   | Enabled   |

A new provider is added by implementing the outbound ports in `domain` and
registering an adapter in `infrastructure` (see §7.3). No changes to
`domain/`, `application/` or `presentation/` are required.

---

## 2. Technology stack

Versions are taken from the parent POM (`spring-boot-starter-parent` **4.0.7**)
and the modules' POMs.

| Concern            | Choice                                                                 |
|--------------------|-------------------------------------------------------------------------|
| Language           | **Java 25** (records, pattern matching, sealed types)                   |
| Runtime            | Spring Boot **4.0.7** (Servlet 6.1 / Spring Framework 7)                 |
| Web (servlet)      | `spring-boot-starter-webmvc` (Tomcat 11)                                |
| Reactive client    | `spring-boot-starter-webflux` (WebClient, used by provider clients)      |
| Security           | Spring Security 7 (`spring-boot-starter-security`)                       |
| Validation         | Jakarta Validation 3.1 (`jakarta.*`)                                    |
| Logging            | Apache **Log4j2** 2.25.4 (via `spring-boot-starter-log4j2`)              |
| JSON               | Jackson 3 (Spring Boot 4 default; `tools.jackson.databind`)             |
| Build              | Maven 3.9+ (multi-module)                                               |
| Test               | JUnit Jupiter 5.11 + AssertJ 3.26 + Mockito 5.18                        |
| Container          | `eclipse-temurin:25`                                                   |

> **Maven coordinates.** `groupId` = **`com.core.service`**; `artifactId`s =
> **`main`**, **`application`**, **`domain`**, **`presentation`**,
> **`infrastructure`**, **`utilities`**. Java packages are
> **`com.core.service.*`**, matching the `groupId`.

---

## 3. Module layout

The reactor (parent POM `<modules>`) is, in build order:

```text
core-integration/                       (parent POM — groupId com.core.service)
├── utilities/          (cross-cutting helpers — logging, string, date)
├── domain/             (innermost layer — pure Java, framework-free)
├── application/        (use cases, depends on domain + Spring context)
├── infrastructure/     (provider adapters, depends on domain + web/webflux)
├── presentation/       (REST controllers, depends on application; owns HTTP security)
└── main/               (Spring Boot entry point — composition root, wires everything)
```

### 3.1 Actual compile-time dependencies

Taken verbatim from each module's `<dependencies>`:

| Module             | Declared dependencies (compile)                                                        |
|--------------------|----------------------------------------------------------------------------------------|
| **utilities**      | `log4j-api`, `log4j-core`                                                              |
| **domain**         | `lombok` (provided), `utilities`                                                       |
| **application**    | `domain`, `spring-context`, `spring-tx`, `utilities`, `lombok` (provided)              |
| **infrastructure** | `domain`, `spring-boot-starter-webmvc`, `spring-boot-starter-webflux`, `utilities`, `spring-boot-configuration-processor`, `lombok` (provided) |
| **presentation**   | `application`, `spring-boot-starter-webmvc`, `spring-boot-starter-security`, `utilities`, `spring-boot-starter-validation`, `lombok` (provided) |
| **main**           | `domain`, `application`, `infrastructure`, `presentation`, `utilities`, `spring-boot-starter-webmvc` (logging excluded), `spring-boot-starter-actuator` (logging excluded), `spring-boot-starter-log4j2`, `lombok` (provided) |

Two facts worth calling out, because they are easy to get wrong:

- **`infrastructure` does _not_ depend on `application`.** Adapters implement the
  port interfaces declared in `domain` directly. The application's
  `ProviderRouter` receives the adapter beans at runtime through Spring
  injection (wired by `main`). Compile-time coupling therefore points only at
  `domain`.
- **`presentation` does _not_ depend on `domain` or `infrastructure`.** It depends
  on `application` (services, DTOs, commands) and never imports a domain or
  infrastructure type.

### 3.2 Dependency flow (inward, with runtime wiring at the root)

```text
                 main  (composition root: @SpringBootApplication, scans com.core.service)
                  │  discovers & wires every @Component/@Service/@Configuration/@RestController
                  │
   ┌──────────────┼───────────────────────────────────────────────┐
   │              │                                                │
   ▼              ▼                                                ▼
presentation   application ─────────────▶ domain ◀──────── infrastructure
   │              │                      ▲                      │
   │              │                      │                      │
   └──────▶ utilities ◀────────────────┴──────────────────────┘
            (used by every module; depends only on Log4j2)
```

At **compile time** the arrows point inward to `domain` (and `utilities`). At
**runtime**, `main` performs the wiring: its component scan over `com.core.service`
picks up the infrastructure adapters (which implement `domain` ports) and injects
them into `application`'s `ProviderRouter`. This keeps the layers decoupled while
letting Spring assemble the full object graph at the composition root.

---

## 4. Domain model

The domain models the **life-cycle of a flight order** in provider-agnostic terms.
It is pure Java — no Spring, no Jackson, no JPA. `domain` depends only on
`utilities` (for helpers) and Lombok.

```text
domain/
├── model/
│   ├── entity/            FlightOffer (record), Itinerary (record), Segment (record),
│   │                       Passenger (record), Order (class — the order representation)
│   ├── valueobject/        AirlineCode, AirportCode, BookingClass, CabinClass,
│   │                       FlightNumber, FlightSearchCriteria, Money, PassengerType, TripType
│   └── enums/             OrderStatus, ProviderType
├── port/out/              FlightSearchProvider, OrderManagementProvider,
│                           PricingProvider, ProviderStrategy
└── exception/             DomainException, ProviderCommunicationException,
                            ProviderNotFoundException, ProviderUnavailableException
```

### 4.1 Entities and value objects

- **`FlightOffer`**, **`Itinerary`**, **`Segment`**, **`Passenger`** are Java
  `record`s. `Itinerary` holds `List<Segment>`; `Segment` holds the flight
  details (flight number, endpoints, times, cabin/booking class, equipment).
- **`Order`** is a plain `class` (the order representation / aggregate root): it
  exposes `orderId`, `providerOrderId`, `providerId`, `passengers`, `offers`,
  `totalPrice`, `status` (`OrderStatus`), `createdAt`, `updatedAt`.
- Value objects are immutable and validate on construction where appropriate
  (e.g. `AirportCode`, `AirlineCode`, `BookingClass` enforce IATA-format
  constraints); `Money` pairs `BigDecimal` + `Currency`.

### 4.2 Outbound ports

The domain defines the contracts the rest of the application depends on; the
infrastructure module supplies the implementations.

| Port                          | Key method(s)                                                                 |
|-------------------------------|-------------------------------------------------------------------------------|
| `FlightSearchProvider`        | `List<FlightOffer> searchFlights(FlightSearchCriteria)`                        |
| `OrderManagementProvider`     | `Order createOrder(OrderCreateRequest)`, `Order retrieveOrder(String)`, `Order cancelOrder(String)` (nested `OrderCreateRequest`, `PaymentInfo`, `enum PaymentMethod`) |
| `PricingProvider`             | `FlightOffer confirmPrice(String offerId, List<Passenger> passengers)`         |
| `ProviderStrategy`            | `ProviderType providerType()`, `default String providerId()` (= `providerType().name()`), `boolean isEnabled()` |

`isEnabled()` is the **single** provider-status method (the former
`isAvailable()` / client `isReachable()` checks were consolidated into it). It
simply reflects the provider's `enabled` configuration flag.

---

## 5. Application layer

The application layer orchestrates use cases. It is `@Service`/`@Component`-driven
and depends on `domain` (ports, models) plus Spring context — not on any web or
infrastructure type.

```text
application/
├── command/                 CreateOrderCommand (nested PassengerData, PaymentData),
│                           RetrieveOrderQuery, SearchFlightsCommand
├── dto/                     FlightOfferDto (nested ItineraryDto/SegmentDto/PriceDto), OrderDto
├── mapper/                  FlightOfferMapper, OrderMapper (static, stateless)
├── base/provider/           ProviderRouter (@Component), ProviderStrategyRegistry<T>
│       exception/            ApplicationException, InvalidCommandException,
│                           ProviderCommunicationException, ProviderNotFoundException,
│                           ProviderUnavailableException
└── service/                 FlightSearchService, OrderService
```

### 5.1 `ProviderRouter` + `ProviderStrategyRegistry` — explicit, no fallback

`ProviderRouter` is a `@Component` that holds three typed
`ProviderStrategyRegistry<T>` instances (one per port) and exposes lookup by
either `ProviderType` or a provider id `String`:

- `getFlightSearchProvider` / `getOrderManagementProvider` / `getPricingProvider`
- `resolveFlightSearchProvider` / `resolveOrderManagementProvider` / `resolvePricingProvider`
- `getAvailableFlightSearchProviderIds/Types` / `getAvailableOrderManagementProviderIds/Types`

`ProviderStrategyRegistry<T extends ProviderStrategy>` builds an
`EnumMap<ProviderType, T>` from the injected provider beans. **There is no
fallback to "the first available provider."** The requested provider is used
explicitly:

- A `null` / blank provider id → `ProviderNotFoundException`.
- An unknown id (no matching `ProviderType`) → `ProviderNotFoundException`.
- A known but **disabled** provider (`isEnabled() == false`) →
  `ProviderUnavailableException`.

`resolve*` and `get*` are equivalent here — both require an explicit provider and
fail fast. The `getAvailable*Ids/Types` methods simply list the **enabled**
providers (used by the `/providers` endpoints).

### 5.2 Application services

- **`FlightSearchService`** — `searchFlights(SearchFlightsCommand)` resolves the
  provider via `resolveFlightSearchProvider(command.preferredProvider())`, maps the
  command to a `FlightSearchCriteria`, calls `provider.searchFlights(...)`, and
  maps the result to `FlightOfferDto` (tagged with `provider.providerId()`).
  `getAvailableProviders()` returns the enabled flight-search provider ids.
  A `DomainException` from the provider is translated to
  `ProviderCommunicationException` (HTTP 502).
- **`OrderService`** — `createOrder(CreateOrderCommand)` resolves the provider via
  `command.providerId()`, builds an `OrderCreateRequest` (nested `Passenger`s +
  `PaymentInfo`), calls `provider.createOrder(...)`, and maps to `OrderDto`.
  `retrieveOrder(RetrieveOrderQuery)` and `cancelOrder(orderId, providerId)` use
  `getOrderManagementProvider(...)`. `getAvailableProviders()` returns the enabled
  order-management provider ids. Provider `DomainException` →
  `ProviderCommunicationException`; an `IllegalStateException` →
  `InvalidCommandException`.

`providerId` (orders) and `preferredProvider` (search) are **mandatory** — there
is no implicit default.

---

## 6. Infrastructure layer

Implements the domain outbound ports for each provider. Per provider there is a
**client** (transport) and **three adapters** (one per port).

```text
infrastructure/
├── exception/
│   └── ProviderCommunicationException.java
└── provider/
    ├── amadeus/
    │   ├── base/      AmadeusClient            (@ConditionalOnProperty, isEnabled())
    │   ├── search/    AmadeusFlightSearchAdapter  implements FlightSearchProvider
    │   ├── order/     AmadeusOrderAdapter        implements OrderManagementProvider
    │   ├── pricing/   AmadeusPricingAdapter      implements PricingProvider
    │   └── AmadeusProperties (@ConfigurationProperties(prefix = "providers.amadeus"))
    └── travelport/
        ├── base/      TravelportClient          (@ConditionalOnProperty, isEnabled())
        ├── search/    TravelportFlightSearchAdapter implements FlightSearchProvider
        ├── order/     TravelportOrderAdapter     implements OrderManagementProvider
        ├── pricing/   TravelportPricingAdapter    implements PricingProvider
        └── TravelportProperties (@ConfigurationProperties(prefix = "providers.travelport"))
```

### 6.1 Conditional registration

Every adapter **and** every client is annotated with
`@ConditionalOnProperty(prefix = "providers.<id>", name = "enabled", havingValue = "true")`.
Setting `providers.amadeus.enabled: false` removes the Amadeus client and all
three Amadeus adapters from the Spring context. `ProviderRouter` then simply has
fewer beans to register, so a misconfigured deployment fails fast rather than
silently dropping a provider.

The clients use the reactive `WebClient` (from `spring-boot-starter-webflux`) for
transport; today they are in-memory mocks shaped to map cleanly onto the real
provider REST contracts.

### 6.2 Adding a provider (recipe)

1. Add a `providers.<id>.enabled` flag (and other `providers.<id>.*` properties) to
   `main/src/main/resources/application.yml`.
2. Create `<id>Properties` as a `@ConfigurationProperties(prefix = "providers.<id>")`
   record.
3. Create `<id>Client` (transport) and three adapters implementing the three
   outbound ports, each annotated with
   `@ConditionalOnProperty(prefix = "providers.<id>", name = "enabled", havingValue = "true")`.
4. Done — `main`'s component scan picks the beans up, `ProviderRouter` registers
   them, and the REST API starts listing `"<ID>"` in `/providers`. The rest of the
   system is unaware anything changed.

---

## 7. Presentation layer

The only module that exposes HTTP. It depends on `application` and translates
between HTTP DTOs and application commands/services.

```text
presentation/
├── controller/
│   ├── FlightSearchController.java    @RequestMapping("/api/v1/flights")
│   └── OrderController.java           @RequestMapping("/api/v1/orders")
├── request/
│   ├── FlightSearchRequest.java  (Jakarta-validated record, compact-ctor defaults)
│   └── CreateOrderRequest.java   (nested PassengerRequest/DocumentRequest/PaymentRequest)
├── response/
│   ├── ErrorResponse.java        (RFC 7807-style)
│   ├── FlightSearchResponse.java
│   └── OrderResponse.java
├── exception/
│   └── GlobalExceptionHandler.java   (@RestControllerAdvice)
└── security/
    ├── ApiKeyAuthenticationFilter.java
    ├── ApiKeyProperties.java
    ├── ApiKeySecurityConfig.java
    └── ApiKeySecurityHandlers.java
```

- **Controllers are thin**: map the request to an application command, delegate to
  the service, map the returned DTO to a response.
- **Validation** is enforced at the boundary with Jakarta Validation annotations
  on the request records (e.g. `origin`/`destination` must be 3-letter IATA codes,
  passenger counts bounded).
- **`GlobalExceptionHandler`** translates known exceptions into a uniform
  `ErrorResponse` (see §7.2).
- **HTTP security** lives here (see §8) — the `SecurityFilterChain` and its
  supporting classes are part of the presentation module, colocated with the
  controllers.

### 7.1 Endpoints

| Method | Path                                | Provider arg        | Purpose                                  |
|--------|-------------------------------------|---------------------|------------------------------------------|
| POST   | `/api/v1/flights/search`            | `preferredProvider` (body) | Search flights (mandatory provider)   |
| GET    | `/api/v1/flights/providers`         | —                   | Enabled flight-search provider ids       |
| POST   | `/api/v1/orders`                    | `providerId` (body) | Create an order (mandatory provider)     |
| GET    | `/api/v1/orders/{orderId}`          | `providerId` (query) | Retrieve an order                        |
| DELETE | `/api/v1/orders/{orderId}`          | `providerId` (query) | Cancel an order                          |
| GET    | `/api/v1/orders/providers`          | —                   | Enabled order-management provider ids    |

Every endpoint above **requires the API key** except the public allow-list in §8.

### 7.2 Error handling (`ErrorResponse` + `GlobalExceptionHandler`)

`ErrorResponse` is a record with `type`, `title`, `status`, `detail`, `path`,
`timestamp`, and an optional `errors` list (each `FieldError` has `field` +
`message`) — an RFC 7807-style envelope. `GlobalExceptionHandler`
(`@RestControllerAdvice`) maps exceptions to it:

| Exception                                | HTTP | Title                              |
|------------------------------------------|------|------------------------------------|
| `MethodArgumentNotValidException`         | 400  | Validation Failed                  |
| `InvalidCommandException` / `IllegalArgumentException` | 400 | Bad Request            |
| `HttpMessageNotReadableException`         | 400  | Malformed Request                  |
| `ProviderNotFoundException`                | 404  | Provider Not Found                 |
| `ProviderUnavailableException`            | 503  | Provider Unavailable               |
| `ProviderCommunicationException`          | 502  | Provider Communication Error        |
| `IllegalStateException`                   | 409  | Conflict                           |
| any other `Exception`                     | 500  | Internal Server Error              |

---

## 8. Boot module (`main`) and HTTP security

`CoreIntegrationApplication` is `@SpringBootApplication` +
`@ConfigurationPropertiesScan("com.core.service")` and scans every module from the
shared root package, acting as the **composition root** that wires the layers.
`application.yml` ships defaults and externalises provider credentials and the API
key via environment variables. `main` produces the executable (repackaged) JAR.

### 8.1 API-key authentication (in the presentation module)

HTTP security is an **interface-layer concern**, so it lives in `presentation`
alongside the controllers — not in the entry point. The `presentation` module
declares `spring-boot-starter-security`; `main` inherits it transitively.

| Class                          | Role                                                                 |
|-------------------------------|----------------------------------------------------------------------|
| `ApiKeyProperties`             | `@ConfigurationProperties(prefix = "api")` → `key` (env `API_KEY`), `headerName` (env `API_HEADER_NAME`, default `X-API-KEY`) |
| `ApiKeyAuthenticationFilter`   | `OncePerRequestFilter`; compares the configured header to `api.key`; on match sets a `ROLE_API` authentication |
| `ApiKeySecurityConfig`         | `@Configuration` declaring the `SecurityFilterChain` (stateless; CSRF / form-login / basic-auth disabled) |
| `ApiKeySecurityHandlers`       | `AuthenticationEntryPoint` + `AccessDeniedHandler`; writes the JSON `ErrorResponse` (401 / 403) via Jackson 3 |

**Public allow-list** (no key required):
`GET /actuator/health`, `GET /actuator/info`, `/error`.
Every other request must be authenticated.

**Security filter-chain flow**

```text
HTTP request
   │
   ▼
SecurityFilterChain
   │  authorizeHttpRequests:
   │     /actuator/health, /actuator/info, /error  → permitAll
   │     anyRequest                                  → authenticated
   ▼
ApiKeyAuthenticationFilter  (OncePerRequestFilter)
   │  reads header (default X-API-KEY); matches api.key → ROLE_API principal
   ▼
AuthorizationFilter
   │  authenticated        → controller
   │  missing / invalid key → AuthenticationEntryPoint  → 401 JSON
   │  forbidden            → AccessDeniedHandler       → 403 JSON
   ▼
Controller
```

The security behaviour is verified by `ApiKeySecurityTest` in the presentation
module — a `@SpringBootTest` (web environment `MOCK`) + `@AutoConfigureMockMvc`
test that wires the controllers and the filter chain together and mocks the
downstream application services.

```yaml
api:
  key: ${API_KEY:dev-only-change-me}        # override via the API_KEY env var in production
  header-name: ${API_HEADER_NAME:X-API-KEY}
```

---

## 9. Utilities module

`utilities` is a framework-light, reusable helper library, depended on by every
module. It depends only on Apache Log4j2 (`log4j-api` + `log4j-core`).

| Class        | Package                          | Helpers                                                              |
|--------------|----------------------------------|----------------------------------------------------------------------|
| `LogUtils`   | `com.core.service.utilities.logging`  | `forClass`, `forName`, `putCorrelationId` / `clearCorrelationId` (Log4j2 `ThreadContext`) |
| `StringUtils`| `com.core.service.utilities.string`  | `isBlank`, `isNotBlank`, `defaultIfBlank`, `truncate`, `randomToken`, `toBase64` |
| `DateUtils`  | `com.core.service.utilities.date`    | `nowUtc`, `todayUtc`, `formatIso`/`parseIso`, `formatDate`/`parseDate` |

### 9.1 Logging with Log4j2

`LogUtils` is built directly on the **Log4j2 API** (`LogManager` / `Logger`).
`putCorrelationId` / `clearCorrelationId` store the correlation id in Log4j2's
`ThreadContext` (its MDC equivalent) so it is attached to every line emitted
during the current thread's processing.

The runnable `main` module uses `spring-boot-starter-log4j2` and **excludes**
`spring-boot-starter-logging`, so the whole application logs through one consistent
Log4j2 backend; SLF4J calls elsewhere (e.g. `GlobalExceptionHandler`) are bridged
to Log4j2 via `log4j-slf4j2-impl`.

The Log4j2 configuration lives in `utilities/src/main/resources/log4j2.xml`:

```xml
<Configuration status="WARN" name="CoreIntegration">
    <Properties>
        <Property name="LOG_PATTERN">%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%t] [%X{correlationId}] %logger{36} - %msg%n</Property>
    </Properties>
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="${LOG_PATTERN}"/>
        </Console>
    </Appenders>
    <Loggers>
        <Logger name="com.core.service" level="INFO" additivity="false">
            <AppenderRef ref="Console"/>
        </Logger>
        <Root level="INFO">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```

Every line therefore carries the `correlationId` (when one has been set), making a
single request traceable across modules.

---

## 10. Configuration reference

All runtime configuration is in `main/src/main/resources/application.yml`.

| Property                                       | Default                                | Purpose                                           |
|------------------------------------------------|----------------------------------------|---------------------------------------------------|
| `server.port`                                  | `8080`                                | HTTP listen port                                  |
| `providers.<id>.enabled`                       | `true`                                 | Toggle a provider's client + adapters in/out      |
| `providers.<id>.base-url`                      | provider-specific                      | Provider endpoint                                 |
| `providers.<id>.branch-code` / `username` / `password` | `${TRAVELPORT_BRANCH_CODE:P7777777}` / `${TRAVELPORT_USERNAME:}` / `${TRAVELPORT_PASSWORD:}` | Travelport credentials (env-injected)   |
| `providers.<id>.client-id` / `client-secret`  | `${AMADEUS_CLIENT_ID:}` / `${AMADEUS_CLIENT_SECRET:}` | Amadeus credentials (env-injected)     |
| `providers.<id>.connection-timeout-ms`        | `5000`                                | HTTP client connect timeout                       |
| `providers.<id>.read-timeout-ms`              | `30000`                               | HTTP client read timeout                          |
| `api.key`                                      | `${API_KEY:dev-only-change-me}`        | API key required on protected endpoints           |
| `api.header-name`                              | `${API_HEADER_NAME:X-API-KEY}`         | Request header that carries the API key           |
| `management.endpoints.web.exposure.include`    | `health,info,metrics`                  | Actuator endpoints exposed                        |
| `management.endpoint.health.show-details`      | `when-authorized`                      | Who may see full health details                   |
| `logging.level.com.core.service`               | `INFO`                                | Log4j2 level for the application package           |

> Note: `application.yml` also sets `logging.pattern.console`; that pattern is
> superseded by the Log4j2 `PatternLayout` in `utilities/src/main/resources/log4j2.xml`,
> which is the effective console layout.

---

## 11. Testing strategy

Tests are layered to match the architecture, so each module is verified at the
narrowest scope that still proves its contract.

| Module          | Style                                                        | Focus                                                                 |
|-----------------|--------------------------------------------------------------|-----------------------------------------------------------------------|
| `utilities/`    | Plain JUnit + AssertJ                                        | String/date helpers; `LogUtils` correlation id via Log4j2 `ThreadContext` |
| `domain/`       | Plain JUnit + AssertJ                                        | Invariants on entities & value objects (continuity, chronology, IATA)   |
| `application/`  | Plain JUnit + Mockito (no Spring context)                    | `ProviderRouter` resolution/fail-fast; service orchestration            |
| `infrastructure/` | Plain JUnit + Mockito                                      | Adapter mapping; mock-client behaviour                                 |
| `presentation/` | `@WebMvcTest` (controllers) and `@SpringBootTest` + `@AutoConfigureMockMvc` (security) | Controllers, `GlobalExceptionHandler`, **API-key security** (downstream services mocked) |
| `main/`         | `@SpringBootTest` (context load)                             | Composition-root smoke test — the fully wired app (incl. security) boots |

Run them with:

```bash
mvn test                 # every module
mvn -pl domain test      # a single module
mvn -pl presentation test # controllers + security slice
```

---

## 12. Key design decisions

1. **Multi-module Maven** — enforces the dependency direction at build time.
2. **Port-Adapter (Hexagonal) architecture** — ports in `domain`, adapters in
   `infrastructure`; the application depends only on the port interfaces, and
   `main` wires the implementations at runtime.
3. **Explicit provider selection, no fallback** — the router uses only the named
   provider and fails fast (404 / 503) when it is missing or disabled.
4. **Single status method** — `ProviderStrategy.isEnabled()` replaces the previous
   `isAvailable()` / `isReachable()` split.
5. **Framework-free domain** — no Spring, no Jackson, no JPA in `domain/`.
6. **Provider-agnostic API** — REST contracts never expose provider-specific
   fields; the provider id is surfaced as a plain string for traceability.
7. **Records for immutable data** — value objects and most entities
   (`FlightOffer`, `Itinerary`, `Segment`, `Passenger`) are Java `record`s; the
   order representation is a mutable `class` where the domain requires it.
8. **Conditional provider registration** — disabling a provider removes its
   client + adapters from the context cleanly (`@ConditionalOnProperty`).
9. **API-key authentication at the edge** — a `OncePerRequestFilter` +
   `SecurityFilterChain` in the presentation layer protects every endpoint while
   leaving a small public allow-list open.
10. **Shared utilities + single Log4j2 backend** — helpers live in one module and
    the application logs through one consistent Log4j2 backend (Logback excluded).
11. **Spring Boot 4.0.x baseline** — tracks the current LTS-grade Spring Boot line
    and follows Spring's recommended starter names.

---

## 13. Aviation domain glossary

| Term                | Meaning                                                                                |
|---------------------|----------------------------------------------------------------------------------------|
| **Offer**           | A priced, bookable combination of itineraries returned by a provider                    |
| **Itinerary**       | One direction of travel (outbound or inbound) composed of contiguous segments          |
| **Segment**         | A single takeoff → landing on a single flight number                                   |
| **PNR** / **Order** | A confirmed booking; the order representation / aggregate root of the booking flow      |
| **Ticketing**       | The act of converting a confirmed order into a flown ticket (status `TICKETED`)        |
| **Validating carrier** | The airline whose ticket stock is used even when segments are operated by partners |
| **GDS**             | Global Distribution System (Travelport, Amadeus, Sabre)                                |
| **NDC**             | New Distribution Capability — IATA's direct-airline XML/JSON standard                  |
