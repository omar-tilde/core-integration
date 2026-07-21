# Core Integration — Architecture Document

> **Contextual memory for the project.** This document is the single source of truth
> for how the application is organised, how its modules talk to each other, and what
> every public-facing concept means. If something in the code disagrees with this
> document, the code wins — and then this document is updated.

---

## 1. What is Core Integration?

**Core Integration** is a **provider-agnostic aviation order & reservation aggregator**.
It exposes a single, unified REST API on top of multiple distribution back-ends so
that downstream consumers (web, mobile, B2B) never have to deal with the quirks of any
particular provider.

Two providers are wired today:

| Provider    | Id           | Status    |
|-------------|--------------|-----------|
| Travelport  | `TRAVELPORT` | Enabled   |
| Amadeus     | `AMADEUS`    | Enabled   |

A new provider (Sabre, Navitaire, direct NDC, …) is added by:

1. Implementing the outbound ports in `domain/port/out/`.
2. Adding an adapter in `infrastructure/` annotated with
   `@ConditionalOnProperty(prefix = "providers.<id>", …)`.

No changes to `domain/`, `application/` or `presentation/` are required.

---

## 2. Technology stack

| Concern            | Choice                                                |
|--------------------|--------------------------------------------------------|
| Language           | **Java 25** (records, pattern matching, sealed types)  |
| Runtime            | Spring Boot **4.0.7** (Servlet 6.1 / Spring Framework 7) |
| Web                | `spring-boot-starter-webmvc` (Tomcat 11)              |
| Reactive client    | `spring-boot-starter-webflux` (WebClient)              |
| Security           | Spring Security 7 (`spring-boot-starter-security`)       |
| Validation         | Jakarta Validation 3.1 (`jakarta.*`)                   |
| Configuration      | Spring Boot ConfigurationProperties + Records          |
| Build              | Maven 3.9+ (multi-module)                              |
| Test               | JUnit Jupiter 5.11 + AssertJ 3.26 + Mockito 5.18       |
| Container          | `eclipse-temurin:25`                                  |
| JSON               | Jackson 3 (Spring Boot 4 default)                      |

> **Maven coordinates.** `groupId` is **`com.core.service`**; the module
> `artifactId`s are **`main`**, **`application`**, **`domain`**,
> **`presentation`**, **`infrastructure`** and **`utilities`**. Java packages
> are **`com.core.service.*`**, matching the Maven `groupId`.

---

## 3. Module layout

```text
core-integration/                       (parent POM — groupId com.core.service)
├── utilities/          (cross-cutting helpers — logging, string, date)
├── domain/             (innermost layer — pure Java)
├── application/        (use cases, depends on domain)
├── infrastructure/     (provider adapters, depends on application)
├── presentation/       (REST controllers, depends on application, owns HTTP security)
└── main/               (Spring Boot entry point — composition root, wires everything)
```

### 3.1 Dependency flow (inward only, utilities everywhere)

```text
   main  ──▶ presentation  ──▶ application  ──▶ domain
                                └─▶ infrastructure ─┘
                              │
              utilities  ◀────┴────────────────────────── (used by every module)
```

The arrows **always point inward**. The domain has **zero** dependencies on Spring,
Jackson, Hibernate or any other framework. Adding a new framework dependency to
`domain/pom.xml` is a build failure waiting to happen. `utilities` depends only on
`slf4j-api` and is depended on by all other modules.

### 3.2 What lives where

| Module             | Responsibility                                                                                   | Talks to                |
|--------------------|--------------------------------------------------------------------------------------------------|-------------------------|
| **utilities**      | Shared, framework-light helpers: `LogUtils`, `StringUtils`, `DateUtils`                            | nothing (slf4j-api only) |
| **domain**         | Entities, value objects, enums, domain services, outbound ports                                  | nothing (pure Java)     |
| **application**    | Use cases, commands, queries, DTOs, mappers, `ProviderRouter`, application services, exceptions  | `domain` only           |
| **infrastructure** | Provider adapters (Travelport, Amadeus), their clients, configuration, exceptions                | `domain`, `application` |
| **presentation**   | REST controllers, request/response DTOs, validation, global exception handler, API-key HTTP security | `application`, `domain` |
| **main**           | `@SpringBootApplication` class, `application.yml`, executable JAR (composition root)            | all of the above        |

---

## 4. Domain model

The domain models the **life-cycle of a flight order** in provider-agnostic terms.

```text
domain/
├── model/
│   ├── entity/
│   │   ├── FlightOffer.java
│   │   ├── Itinerary.java
│   │   ├── Order.java
│   │   ├── Passenger.java
│   │   └── Segment.java
│   ├── valueobject/
│   │   ├── AirlineCode.java
│   │   ├── AirportCode.java
│   │   ├── BookingClass.java
│   │   ├── CabinClass.java
│   │   ├── FlightNumber.java
│   │   ├── FlightSearchCriteria.java
│   │   ├── Money.java
│   │   ├── PassengerType.java
│   │   └── TripType.java
│   └── enums/
│       └── OrderStatus.java
├── port/
│   └── out/
│       ├── FlightSearchProvider.java
│       ├── OrderManagementProvider.java
│       └── PricingProvider.java
└── (domain services live here when needed)
```

### 4.1 Aggregates and value objects

- `Order` is the **aggregate root** of the booking. All mutations
  (`confirm`, `ticket`, `cancel`, `refund`) go through it and respect
  the legal state transitions.
- `Itinerary` is an ordered list of `Segment`s, validated for **continuity**
  (destination of segment N must equal origin of segment N+1) and
  **chronology** (segment N+1 must depart after segment N arrives).
- `Money` is an immutable pair of `BigDecimal` and `Currency` that scales
  itself to the currency's default fraction digits.
- `AirportCode`, `AirlineCode`, `BookingClass` validate IATA format on
  construction; they cannot exist in an invalid state.

### 4.2 Outbound ports and provider status

The domain defines the contracts that the rest of the application depends on.
The infrastructure module supplies one or more implementations per port.

| Port                          | Purpose                                                          |
|-------------------------------|------------------------------------------------------------------|
| `FlightSearchProvider`        | Shop for flight offers based on `FlightSearchCriteria`           |
| `OrderManagementProvider`     | Create / retrieve / cancel orders, capture payment intent        |
| `PricingProvider`             | Re-price an offer for a specific passenger list                  |

Every provider strategy implements `ProviderStrategy`, whose single status method is
**`isEnabled()`** (the former `isAvailable()` / client `isReachable()` checks were
consolidated into this one method). `isEnabled()` simply reflects the provider's
`enabled` configuration flag. A provider is identified by a stable, upper-case string
(e.g. `"AMADEUS"`).

---

## 5. Application layer

```text
application/
├── command/                 # Use-case input objects (immutable records)
│   ├── CreateOrderCommand.java
│   ├── RetrieveOrderQuery.java
│   └── SearchFlightsCommand.java
├── dto/                     # Use-case output objects
│   ├── FlightOfferDto.java
│   └── OrderDto.java
├── mapper/                  # Domain ⇄ DTO mappers (static, stateless)
│   ├── FlightOfferMapper.java
│   └── OrderMapper.java
├── provider/                # Provider-routing infrastructure
│   ├── ProviderRouter.java
│   ├── ProviderNotFoundException.java
│   └── ProviderUnavailableException.java
├── service/                 # Application services (one per use case family)
│   ├── FlightSearchService.java
│   └── OrderService.java
└── (exception base package)
```

### 5.1 `ProviderRouter` — explicit selection, no fallback

The router is a **strategy selector**. It accepts lists of `FlightSearchProvider`,
`OrderManagementProvider` and `PricingProvider` beans (Spring injects all of them) and
exposes lookup by provider id/type.

**There is no fallback to "the first available provider".** The application uses
**only the explicitly requested provider**:

- `getProvider(...)` / `resolveProvider(...)` require a non-blank provider id/type.
- If the requested provider is **missing or cannot be resolved** →
  `ProviderNotFoundException` (HTTP 404) with a message such as
  `"Requested flight search provider 'SABRE' is not available."`.
- If the requested provider is **present but disabled** (`isEnabled() == false`) →
  `ProviderUnavailableException` (HTTP 503).
- `getAvailableProviderIds()` / `getAvailableProviderTypes()` still list the
  currently **enabled** providers (used by the `/providers` endpoints) — these are
  read-only listings, not fallback logic.

The now-removed `getFirstAvailableProvider()` method and the
`NoProviderAvailableException` type have been deleted; requests fail fast instead of
silently switching providers.

### 5.2 Application services

- `FlightSearchService` maps a `SearchFlightsCommand` to a `FlightSearchCriteria`,
  asks the **resolved** `FlightSearchProvider` for offers, and maps them back to
  `FlightOfferDto`. `preferredProvider` is mandatory.
- `OrderService` orchestrates order creation, retrieval, and cancellation. It is the
  only place that converts a `CreateOrderCommand` (and its nested payment/passenger
  data) into an `OrderCreateRequest` that the provider port can consume.
  `providerId` is mandatory for create, retrieve and cancel.

---

## 6. Infrastructure layer

```text
infrastructure/
├── exception/
│   └── ProviderCommunicationException.java
└── provider/
    ├── travelport/
    │   ├── TravelportClient.java
    │   ├── TravelportFlightSearchAdapter.java
    │   ├── TravelportOrderAdapter.java
    │   ├── TravelportPricingAdapter.java
    │   └── TravelportProperties.java
    └── amadeus/
        ├── AmadeusClient.java
        ├── AmadeusFlightSearchAdapter.java
        ├── AmadeusOrderAdapter.java
        ├── AmadeusPricingAdapter.java
        └── AmadeusProperties.java
```

### 6.1 Adapter pattern

Each provider has a **client** (the transport layer) and three **adapters** that
implement the outbound ports. Today the clients are in-memory mocks; the public
HTTP contracts (`AmadeusFlightOfferResponse`, `TravelportFlightResponse`, …) are
already shaped to map cleanly to the real REST responses.

Each adapter's `isEnabled()` delegates to its client's `isEnabled()`, which returns
the provider's `enabled` configuration flag.

### 6.2 Conditional registration

Each adapter/client is annotated with
`@ConditionalOnProperty(prefix = "providers.<id>", name = "enabled", havingValue = "true")`.
Flipping `providers.amadeus.enabled: false` in `application.yml` removes Amadeus from
the application context without code changes. The `ProviderRouter` defensively filters
out `null` beans so a misconfigured deployment fails fast instead of silently
dropping providers.

### 6.3 Adding a provider (recipe)

1. Add a `provider.<id>.enabled` flag to `application.yml`.
2. Create a `<id>Properties` record annotated with
   `@ConfigurationProperties(prefix = "providers.<id>")`.
3. Create `<id>Client` (transport) and three adapters implementing the three outbound
   ports, each annotated with `@ConditionalOnProperty(prefix = "providers.<id>", …)`.
4. (Optional) Add unit tests under
   `infrastructure/src/test/java/com/core/service/infrastructure/provider/<id>/`.

---

## 7. Presentation layer

```text
presentation/
├── controller/
│   ├── FlightSearchController.java
│   └── OrderController.java
├── request/
│   ├── CreateOrderRequest.java
│   └── FlightSearchRequest.java
├── response/
│   ├── ErrorResponse.java
│   ├── FlightSearchResponse.java
│   └── OrderResponse.java
└── exception/
    └── GlobalExceptionHandler.java
```

- Controllers are **thin**: they translate between HTTP DTOs and
  application commands, and they delegate to the application services.
- Validation is enforced at the boundary via Jakarta Validation
  annotations on the request records.
- `GlobalExceptionHandler` converts known exceptions into a uniform
  `ErrorResponse` (RFC 7807-style). Unknown exceptions become `500`.
- **HTTP security** (API-key authentication) is owned here too: the
  `SecurityFilterChain` and its supporting filter/properties/handlers live in the
  `presentation/security` package, so the web boundary — controllers and auth — are
  colocated. See §8.1.

---

## 8. Boot module (`main`)

The boot module is the **composition root**: the only place that produces an
executable JAR and wires every layer together. It is intentionally minimal and
holds **no business or web logic of its own** — security included:

- `CoreIntegrationApplication` carries `@SpringBootApplication` and
  `@ConfigurationPropertiesScan("com.core.service")`, scanning every module from the
  shared root package.
- `application.yml` ships sensible defaults and externalises provider
  credentials and the API key via environment variables.

### 8.1 Presentation-layer security (API key authentication)

HTTP security is an **interface-layer concern**, so it lives in the `presentation`
module alongside the REST controllers — not in the entry point. This keeps `main` a
pure composition root and lets the security filter chain be verified with a focused
`@WebMvcTest` slice in the presentation module. The `presentation` module declares the
`spring-boot-starter-security` dependency; `main` inherits it transitively.

- `presentation/security/ApiKeyAuthenticationFilter` reads the key from a configurable
  request header (default `X-API-KEY`) and, when it matches the configured `api.key`,
  establishes an authenticated `SecurityContext`.
- `presentation/security/ApiKeySecurityConfig` declares the `SecurityFilterChain` bean,
  makes the filter chain **stateless**, disables CSRF / form login / basic auth, and
  requires authentication on every request except an explicit public allow-list:
  - `GET /actuator/health`
  - `GET /actuator/info`
  - `/error`
- `presentation/security/ApiKeySecurityHandlers` replaces Spring Security's default
  responses with the application's standard JSON `ErrorResponse`: **401 Unauthorized**
  for missing/invalid keys, **403 Forbidden** otherwise.
- `presentation/security/ApiKeyProperties` binds `api.key` (env `API_KEY`) and
  `api.header-name` (env `API_HEADER_NAME`). The key is read from configuration only
  — it is never hard-coded in Java.

```yaml
api:
  key: ${API_KEY:dev-only-change-me}        # override via the API_KEY env var in production
  header-name: ${API_HEADER_NAME:X-API-KEY}
```

---

## 9. Utilities module

`utilities` is a framework-light, reusable helper library shared by every module.

| Class                          | Package                          | Helpers                                                        |
|--------------------------------|----------------------------------|----------------------------------------------------------------|
| `LogUtils`                     | `com.core.service.utilities.logging`| `forClass`, `forName`, MDC `putCorrelationId` / `clearCorrelationId` |
| `StringUtils`                  | `com.core.service.utilities.string` | `isBlank`, `defaultIfBlank`, `truncate`, `randomToken`, `toBase64`   |
| `DateUtils`                    | `com.core.service.utilities.date`   | `nowUtc`, `todayUtc`, ISO `format`/`parse`, pattern `format`/`parse` |

The module depends only on Apache Log4j2 (`log4j-api` + `log4j-core`, versions
managed by the Spring Boot BOM) and is depended on by `domain`, `application`,
`infrastructure`, `presentation` and `main`, so the helpers are available throughout
the codebase without re-implementing them per module. `LogUtils` is built directly on
the Log4j2 API; the runnable `main` module uses `spring-boot-starter-log4j2` so the
whole application logs through one consistent Log4j2 backend (SLF4J calls are bridged
via `log4j-slf4j2-impl`). A Log4j2 configuration lives in this module's
`src/main/resources/log4j2.xml`.

---

## 10. Key design decisions

1. **Multi-module Maven** — enforces the dependency direction at build time.
2. **Port-Adapter (Hexagonal) architecture** — ports in domain, adapters in
   infrastructure; the application layer never imports an adapter type.
3. **Explicit provider selection** — the router uses only the named provider and
   fails fast (no fallback) when it is missing, disabled, or unresolvable.
4. **Single status method** — `ProviderStrategy.isEnabled()` replaces the previous
   `isAvailable()` / `isReachable()` split, keeping naming consistent.
5. **Framework-free domain** — no Spring, no Jackson, no JPA in `domain/`.
6. **Provider-agnostic API** — REST contracts never expose provider-specific
   fields. The provider id is surfaced as a plain string for traceability.
7. **Records everywhere** — value objects, DTOs, commands, properties are
   Java `record`s. Mutability only exists where the domain requires it
   (e.g. the `Order` aggregate root, which has a lifecycle).
8. **Conditional provider registration** — disabling a provider removes
   it from the context cleanly (`@ConditionalOnProperty`).
9. **API key authentication** — a simple, maintainable `OncePerRequestFilter` +
   `SecurityFilterChain` protects every endpoint while leaving a small public
   allow-list open.
10. **Shared utilities module** — logging/string/date helpers live in one place and
    are reused project-wide, avoiding duplication.
11. **Spring Boot 4.0.x baseline** — the project tracks the current LTS-grade
    Spring Boot line and follows Spring's recommended starter names
    (`spring-boot-starter-webmvc`, `spring-boot-starter-webmvc-test`, …).

---

## 11. Aviation domain glossary

| Term                | Meaning                                                                                |
|---------------------|----------------------------------------------------------------------------------------|
| **Offer**           | A priced, bookable combination of itineraries returned by a provider                    |
| **Itinerary**       | One direction of travel (outbound or inbound) composed of contiguous segments          |
| **Segment**         | A single takeoff → landing on a single flight number                                   |
| **PNR** / **Order** | A confirmed booking; the aggregate root of the booking flow                            |
| **Ticketing**       | The act of converting a confirmed order into a flown ticket (status `TICKETED`)        |
| **Validating carrier** | The airline whose ticket stock is used even when segments are operated by partners |
| **GDS**             | Global Distribution System (Travelport, Amadeus, Sabre)                                |
| **NDC**             | New Distribution Capability — IATA's direct-airline XML/JSON standard                  |
