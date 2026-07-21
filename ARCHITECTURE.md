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
2. Adding a Spring configuration class in `infrastructure/config/` that wires the
   new adapter under `@ConditionalOnProperty(prefix = "providers.<id>", …)`.

No changes to `domain/`, `application/` or `presentation/` are required.

---

## 2. Technology stack

| Concern            | Choice                                                |
|--------------------|--------------------------------------------------------|
| Language           | **Java 25** (records, pattern matching, sealed types)  |
| Runtime            | Spring Boot **4.0.7** (Servlet 6.1 / Spring Framework 7) |
| Web                | `spring-boot-starter-webmvc` (Tomcat 11)              |
| Reactive client    | `spring-boot-starter-webflux` (WebClient)              |
| Validation         | Jakarta Validation 3.1 (`jakarta.*`)                   |
| Configuration      | Spring Boot ConfigurationProperties + Records          |
| Build              | Maven 3.9+ (multi-module)                              |
| Test               | JUnit Jupiter 5.11 + AssertJ 3.26 + Mockito 5.18       |
| Container          | `eclipse-temurin:25`                                  |
| JSON               | Jackson 3 (Spring Boot 4 default)                      |

> **Why Jackson 3?** Spring Boot 4 ships Jackson 3 as the default. The classic Jackson
> 2 bridge (`spring-boot-jackson2`) is still available for projects that need a
> longer migration runway. The annotations in `com.fasterxml.jackson.annotation`
> remain unchanged and are shared between Jackson 2 and 3.

---

## 3. Module layout

```
core-integration/                       (parent POM — groupId com.coreorder)
├── domain/                             (innermost layer — pure Java)
├── application/                        (use cases, depends on domain)
├── infrastructure/                     (provider adapters, depends on application)
├── presentation/                       (REST controllers, depends on application)
└── main/                               (Spring Boot entry point, wires everything)
```

### 3.1 Dependency flow (inward only)

```
   main  ──▶ presentation  ──▶ application  ──▶ domain
                                └─▶ infrastructure ─┘
```

The arrows **always point inward**. The domain has **zero** dependencies on Spring,
Jackson, Hibernate or any other framework. Adding a new framework dependency to
`domain/pom.xml` is a build failure waiting to happen.

### 3.2 What lives where

| Module             | Responsibility                                                                                   | Talks to                |
|--------------------|--------------------------------------------------------------------------------------------------|-------------------------|
| **domain**         | Entities, value objects, enums, domain services, outbound ports                                  | nothing (pure Java)     |
| **application**    | Use cases, commands, queries, DTOs, mappers, `ProviderRouter`, application services, exceptions  | `domain` only           |
| **infrastructure** | Provider adapters (Travelport, Amadeus), their clients, configuration, registry, exceptions     | `domain`, `application` |
| **presentation**   | REST controllers, request/response DTOs, validation, global exception handler                     | `application`, `domain` |
| **main**           | `@SpringBootApplication` class, `application.yml`, executable JAR                               | all of the above        |

---

## 4. Domain model

The domain models the **life-cycle of a flight order** in provider-agnostic terms.

```
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

### 4.2 Outbound ports

The domain defines the contracts that the rest of the application depends on.
The infrastructure module supplies one or more implementations per port.

| Port                          | Purpose                                                          |
|-------------------------------|------------------------------------------------------------------|
| `FlightSearchProvider`        | Shop for flight offers based on `FlightSearchCriteria`           |
| `OrderManagementProvider`     | Create / retrieve / cancel orders, capture payment intent        |
| `PricingProvider`             | Re-price an offer for a specific passenger list                  |

A provider is identified by a stable, upper-case string (e.g. `"AMADEUS"`).
Two providers that disagree on identifier collide at the `ProviderRouter`
and the application fails fast at boot time.

---

## 5. Application layer

```
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
│   ├── ProviderUnavailableException.java
│   └── NoProviderAvailableException.java
├── service/                 # Application services (one per use case family)
│   ├── FlightSearchService.java
│   └── OrderService.java
└── config/
    └── ApplicationConfiguration.java
```

### 5.1 `ProviderRouter`

The router is a **strategy selector**. It accepts:

- A list of `FlightSearchProvider` beans (Spring injects all of them).
- A list of `OrderManagementProvider` beans.
- A list of `PricingProvider` beans.

It exposes lookup by provider id and a "preferred or first available" resolver.
This is where the application decides which provider actually answers a request.

### 5.2 Application services

- `FlightSearchService` maps a `SearchFlightsCommand` to a
  `FlightSearchCriteria`, asks the resolved `FlightSearchProvider` for offers,
  and maps them back to `FlightOfferDto`.
- `OrderService` orchestrates order creation, retrieval, and cancellation.
  It is the only place that knows how to convert a `CreateOrderCommand`
  (and its nested payment/passenger data) into a `OrderCreateRequest`
  that the provider port can consume.

### 5.3 Configuration

`ApplicationConfiguration` is a `@Configuration` class that defines beans
for the router and the two services. It is intentionally tiny; all
real wiring lives in the infrastructure layer.

---

## 6. Infrastructure layer

```
infrastructure/
├── config/
│   └── ProviderInfrastructureConfiguration.java
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
HTTP contracts (`AmadeusFlightOfferResponse`, `TravelportFlightResponse`, …)
are already shaped to map cleanly to the real REST responses.

### 6.2 Conditional registration

`ProviderInfrastructureConfiguration` declares every provider bean with
`@ConditionalOnProperty(prefix = "providers.<id>", name = "enabled", havingValue = "true")`.
Flipping `providers.amadeus.enabled: false` in `application.yml` removes
Amadeus from the application context without code changes.

`ProviderRouter` defensively filters out `null` beans so that a misconfigured
deployment fails fast instead of silently dropping providers.

### 6.3 Adding a provider (recipe)

1. Add a `provider.<id>.enabled` flag to `application.yml`.
2. Create a `<id>Properties` record annotated with
   `@ConfigurationProperties(prefix = "providers.<id>")`.
3. Create `<id>Client` (transport) and three adapters implementing the
   three outbound ports.
4. Register everything in `ProviderInfrastructureConfiguration` behind
   `@ConditionalOnProperty`.
5. (Optional) Add unit tests under
   `infrastructure/src/test/java/com/coreorder/infrastructure/provider/<id>/`.

---

## 7. Presentation layer

```
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

---

## 8. Boot module (`main`)

The boot module is the only place that produces an executable JAR.
It is intentionally minimal:

- `CoreIntegrationApplication` carries `@SpringBootApplication` and
  `@ConfigurationPropertiesScan("com.coreorder")`.
- `application.yml` ships sensible defaults and externalises provider
  credentials via environment variables.

The parent POM is configured to apply `spring-boot-maven-plugin` only on
this module.

---

## 9. Key design decisions

1. **Multi-module Maven** — enforces the dependency direction at build time.
2. **Port-Adapter (Hexagonal) architecture** — ports in domain, adapters in
   infrastructure; the application layer never imports an adapter type.
3. **Provider Router** — strategy pattern. The router is the only place
   that maps a `providerId` to an adapter instance.
4. **Framework-free domain** — no Spring, no Jackson, no JPA in `domain/`.
5. **Provider-agnostic API** — REST contracts never expose provider-specific
   fields. The provider id is surfaced as a plain string for traceability.
6. **Records everywhere** — value objects, DTOs, commands, properties are
   Java `record`s. Mutability only exists where the domain requires it
   (e.g. the `Order` aggregate root, which has a lifecycle).
7. **Conditional provider registration** — disabling a provider removes
   it from the context cleanly (`@ConditionalOnProperty`).
8. **Spring Boot 4.0.x baseline** — the project tracks the current LTS-grade
   Spring Boot line and follows Spring's recommended starter names
   (`spring-boot-starter-webmvc`, `spring-boot-starter-webmvc-test`, …).

---

## 10. Aviation domain glossary

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
