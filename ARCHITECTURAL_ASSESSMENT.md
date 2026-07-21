# Comprehensive Architectural Assessment Report

**System Name:** Core Integration (Aviation Order & Reservation Aggregator)  
**Architect:** Principal Java Software Architect  
**Date:** July 21, 2026  

---

## Executive Summary

This report delivers a thorough architectural assessment of the `core-integration` repository prior to any refactoring. The system is built as a multi-module Maven project intended to follow Clean Architecture (Hexagonal Architecture) principles, exposing a provider-agnostic REST API on top of multiple distribution channels (Travelport, Amadeus).

While the system exhibits strong domain boundaries in `domain/` and clean REST controllers in `presentation/`, several architectural violations, configuration anti-patterns, and code smells exist that impede maintainability, extensibility, and strict alignment with Clean Architecture.

---

## 1. Maven Module Dependency Graph

### Current Declared Dependencies

```
core-integration-parent (POM)
 ├── domain (Pure Java)
 ├── application ──▶ domain
 ├── infrastructure ──▶ domain, application  <-- VIOLATION!
 ├── presentation ──▶ application, domain
 └── main ──▶ domain, application, infrastructure, presentation
```

### Visual Dependency Direction

```
                 ┌──────────────┐
                 │ presentation │
                 └──────┬───────┘
                        │
                        ▼
                 ┌──────────────┐      ┌────────────────┐
                 │ application  │◀─────│ infrastructure │  <-- Invalid
                 └──────┬───────┘      └──────┬─────────┘
                        │                     │
                        ▼                     │
                 ┌──────────────┐             │
                 │    domain    │◀────────────┘
                 └──────────────┘
```

---

## 2. Layer Responsibilities

| Module | Purpose & Components | Allowed Dependencies |
| :--- | :--- | :--- |
| **domain** | Aggregates (`Order`, `Itinerary`), Value Objects (`AirportCode`, `Money`, `FlightSearchCriteria`), Ports (`FlightSearchProvider`, `OrderManagementProvider`, `PricingProvider`). Pure Java. | None (No frameworks) |
| **application** | Use cases, commands, DTOs, mappers (`FlightOfferMapper`, `OrderMapper`), services (`FlightSearchService`, `OrderService`), strategy router (`ProviderRouter`). | `domain`, `spring-context` |
| **infrastructure** | Technical adapters (`AmadeusFlightSearchAdapter`, `TravelportOrderAdapter`, etc.), HTTP clients (`AmadeusClient`, `TravelportClient`), configuration properties. | `domain` **ONLY** |
| **presentation** | REST controllers (`FlightSearchController`, `OrderController`), requests/responses, Jakarta validation, global exception handling (`GlobalExceptionHandler`). | `application`, `domain` |
| **main** | Application root (`CoreIntegrationApplication`), runtime YAML configuration, packaging assembly. | All inner modules |

---

## 3. Clean Architecture Violations

1. **Infrastructure Depending on Application (`infrastructure` ──▶ `application`):**
   - In `infrastructure/pom.xml`, the `application` module is explicitly declared as a dependency.
   - The adapters in `infrastructure` import `com.coreorder.application.provider.ProviderCommunicationException`.
   - In Hexagonal / Clean Architecture, `infrastructure` implements outbound ports defined in `domain`. It must **never** depend on `application`.
2. **Exception Scope Misplacement:**
   - Provider interaction exceptions (such as `ProviderCommunicationException`) are housed in the `application` layer (`com.coreorder.application.provider`) instead of `domain` or `domain.port.out`. This forced `infrastructure` to depend on `application`.

---

## 4. `@Configuration` Classes Used Only for Bean Registration

1. **`ApplicationConfiguration` (`application/config/ApplicationConfiguration.java`):**
   - Used solely to declare `@Bean` factory methods for `ProviderRouter`, `FlightSearchService`, and `OrderService`.
   - These classes can be annotated directly with `@Component` / `@Service` using implicit constructor injection.
2. **`ProviderInfrastructureConfiguration` (`infrastructure/config/ProviderInfrastructureConfiguration.java`):**
   - Manually instantiates 8 `@Bean` instances for `TravelportClient`, `AmadeusClient`, and their respective 6 adapters (`FlightSearch`, `Order`, `Pricing`).
   - Obsoletes automatic Spring component discovery and introduces boilerplate factory methods.

---

## 5. Infrastructure Adapters to be Converted to `@Component` / `@Service`

The following classes are currently instantiated via manual `@Bean` methods and should be annotated directly with Spring stereotype annotations (`@Component`) and conditional properties:

- `AmadeusClient`
- `AmadeusFlightSearchAdapter`
- `AmadeusOrderAdapter`
- `AmadeusPricingAdapter`
- `TravelportClient`
- `TravelportFlightSearchAdapter`
- `TravelportOrderAdapter`
- `TravelportPricingAdapter`

---

## 6. Strategy Pattern Weaknesses & String Keys

1. **Weak Typing using Raw `String` Identifiers:**
   - Strategy lookup in `ProviderRouter` maps adapters by `String` (`"AMADEUS"`, `"TRAVELPORT"`).
   - Domain ports (`FlightSearchProvider`, `OrderManagementProvider`, `PricingProvider`) specify `String providerId()`.
   - Risk of runtime typos, case-sensitivity issues, and lack of compiler checks.
2. **Duplicate Lookup Logic:**
   - `ProviderRouter` contains three sets of identical boilerplate stream filtering, map assembly, lookup, resolution, and fallback logic for each provider port.
3. **Violation of Open/Closed Principle:**
   - Adding a new outbound port or new strategy requiring lookup would require duplicating all strategy mapping methods inside `ProviderRouter`.

---

## 7. Cross-Module Dependency Violations & Duplicate Registrations

- **Cross-module coupling:** `infrastructure` ──▶ `application` via `ProviderCommunicationException`.
- **Boilerplate Factory Methods:** Every new supplier (e.g., Sabre, Navitaire) currently requires adding 4 duplicate `@Bean` declarations in `ProviderInfrastructureConfiguration`.

---

## 8. Circular Dependency Risks

While no compile-time cycle currently exists between `application` and `infrastructure`, the POM dependency `infrastructure` ──▶ `application` combined with `main` importing both creates a structural loop risk. Removing `application` from `infrastructure/pom.xml` guarantees strict acyclic compilation.

---

## 9. Architectural Smells & Recommendations

1. **Lack of Enums for Domain Concepts:**
   - Missing strongly-typed `ProviderType` / `SupplierType` enum in the domain model.
2. **Manual Bean Plumbing:**
   - Prefer component scanning + constructor injection over manual configuration classes.
3. **Generic Strategy Resolution:**
   - Introduce a generic `ProviderStrategyRegistry<T extends ProviderStrategy>` in `application` to encapsulate strategy selection, availability checks, and fallback mechanisms cleanly.

---
