# Core Integration

> **Provider-agnostic aviation order & reservation aggregator.**
> A single, unified REST API on top of multiple distribution providers
> (Travelport, Amadeus, …) so downstream consumers never see the quirks of
> any one GDS or NDC endpoint.

[![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/25/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.7-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Build](https://img.shields.io/badge/build-Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

---

## ✨ Features

- 🔌 **Plug-in provider model** — Travelport and Amadeus ship today;
  adding Sabre/Navitaire/direct NDC is a one-package operation.
- 🧱 **Clean / Hexagonal architecture** — the domain has **zero**
  framework dependencies, the application layer orchestrates use cases,
  the infrastructure layer talks to the outside world, and the
  presentation layer is the only thing that exposes HTTP.
- 🛡️ **Provider-agnostic REST contract** — the API never leaks
  provider-specific fields; the `providerId` is the only traceability hook.
- ✅ **Battle-tested validation** — Jakarta Validation at the boundary,
  invariant enforcement in the domain layer.
- 🔐 **API key authentication** — every application endpoint requires a
  valid API key; only a small public allow-list (`/actuator/health`,
  `/actuator/info`, `/error`) stays open.
- 🎯 **Explicit provider selection** — requests use only the provider they
  name and fail fast when it is missing, disabled, or unresolvable. There is
  no silent fallback to another provider.
- 🧰 **Shared utilities module** — logging, string and date helpers are
  available to every module.
- 🩺 **Spring Boot Actuator** — `health`, `info`, `metrics` out of the box.
- 🐳 **Production-grade container image** — multi-stage Dockerfile based on
  `eclipse-temurin:25`.

---

## 🧰 Tech stack

| Layer        | Technology                                                |
|--------------|-----------------------------------------------------------|
| Language     | **Java 25** (records, pattern matching)                   |
| Framework    | **Spring Boot 4.0.7** + Spring Framework 7                |
| Web          | `spring-boot-starter-webmvc` (Tomcat 11)                  |
| Reactive     | `spring-boot-starter-webflux` (WebClient)                 |
| Security     | `spring-boot-starter-security` (Spring Security 7)        |
| Validation   | Jakarta Validation 3.1                                    |
| JSON         | Jackson 3 (managed by Spring Boot 4)                      |
| Build        | Maven 3.9+ (multi-module)                                 |
| Test         | JUnit 5.11, AssertJ 3.26, Mockito 5.18, Spring Boot Test  |
| Container    | `eclipse-temurin:25-jre`                                  |

> **Maven coordinates.** The project `groupId` is **`com.core.service`** and the
> module `artifactId`s are **`main`**, **`application`**, **`domain`**,
> **`presentation`**, **`infrastructure`** and **`utilities`**. Java packages
> are **`com.core.service.*`**, matching the Maven `groupId`.

---

## 📁 Project structure

```text
core-integration/
├── utilities/          # Cross-cutting helpers: logging, string, date
├── domain/             # Pure Java: entities, value objects, enums, ports
├── application/        # Use cases, commands, DTOs, mappers, router, services
├── infrastructure/     # Provider adapters (Travelport, Amadeus) + config
├── presentation/       # REST controllers, request/response, exception handler, HTTP security
└── main/               # @SpringBootApplication + application.yml (composition root)
```

The dependency graph is strictly **inward**, and every module also depends on
`utilities`:

```text
                 utilities  ◀───────────────────────────────┐
                    ▲     ▲     ▲     ▲     ▲                │
                    │     │     │     │     │                │
   main ─▶ presentation ─▶ application ─▶ domain            │
       └────────────────▶ infrastructure ─┘                 │
                                                         (utilities used by all)
```

> 📘 For a deep dive, see [`ARCHITECTURE.md`](ARCHITECTURE.md).

---

## 🚀 Getting started

### Prerequisites

| Tool         | Version       | Notes                                       |
|--------------|---------------|---------------------------------------------|
| JDK          | **25**        | `java -version` must report 25+              |
| Maven        | **3.9+**      | `mvn -version`                              |
| Docker       | 24+ (optional)| only required for the container workflow     |

### Build

```bash
mvn clean verify
```

This compiles every module, runs all unit tests, and produces the bootable
JAR at `main/target/core-integration-*.jar`.

### Run locally

```bash
# Set the API key the service will require on every protected endpoint
export API_KEY=dev-only-change-me

# Either from the JAR:
java -jar main/target/core-integration-*.jar

# Or directly from Maven:
mvn -pl main spring-boot:run
```

The service listens on **port 8080** by default. Health is public:

```bash
curl -s http://localhost:8080/actuator/health | jq
```

### Run with Docker

```bash
docker build -t core-integration:1.0.0 .
docker run --rm -p 8080:8080 -e API_KEY=dev-only-change-me core-integration:1.0.0
```

---

## ⚙️ Configuration

All runtime configuration is in `main/src/main/resources/application.yml`.
The most relevant knobs are:

| Property                          | Default                                   | Purpose                                      |
|-----------------------------------|-------------------------------------------|----------------------------------------------|
| `server.port`                     | `8080`                                    | HTTP listen port                             |
| `providers.travelport.enabled`    | `true`                                    | Toggle the Travelport adapter                |
| `providers.amadeus.enabled`       | `true`                                    | Toggle the Amadeus adapter                   |
| `providers.travelport.base-url`   | `https://api.travelport.com/universal`    | Travelport endpoint                          |
| `providers.amadeus.base-url`      | `https://api.amadeus.com/v2`              | Amadeus endpoint                             |
| `api.key`                         | `${API_KEY:dev-only-change-me}`           | API key required on protected endpoints      |
| `api.header-name`                 | `${API_HEADER_NAME:X-API-KEY}`            | Request header that carries the API key      |
| `management.endpoints.web.exposure.include` | `health,info,metrics`           | Actuator endpoints exposed                   |

Provider credentials are read from environment variables (with empty defaults
suitable for the in-memory mock clients):

```bash
export TRAVELPORT_USERNAME=...
export TRAVELPORT_PASSWORD=...
export TRAVELPORT_BRANCH_CODE=...
export AMADEUS_CLIENT_ID=...
export AMADEUS_CLIENT_SECRET=...
```

---

## 🔐 API key authentication

Every application endpoint is protected by a shared API key:

- The key is sent in the `X-API-KEY` header (configurable via `api.header-name`
  / `API_HEADER_NAME`).
- The expected key is read from `api.key`, which binds from the `API_KEY`
  environment variable (or `application.yml`). It is **never** hard-coded in
  Java.
- A valid key establishes an authenticated request; missing or invalid keys are
  rejected with **401 Unauthorized** and a JSON `ErrorResponse`. Forbidden
  accesses return **403 Forbidden**.
- The following paths are public and require no key:
  - `GET /actuator/health`
  - `GET /actuator/info`
  - `/error`

Example (valid key):

```bash
curl -s -X POST http://localhost:8080/api/v1/flights/search \
  -H "X-API-KEY: $API_KEY" \
  -H 'Content-Type: application/json' \
  -d '{ "tripType": "ONE_WAY", "origin": "CDG", "destination": "JFK",
        "departureDate": "2026-08-15", "adultCount": 1, "cabinClass": "ECONOMY",
        "maxResults": 50, "preferredProvider": "AMADEUS" }' | jq
```

---

## 🌐 HTTP API (v1)

All endpoints return JSON. Errors follow an
[RFC 7807](https://datatracker.ietf.org/doc/html/rfc7807)-style
`ErrorResponse` with `type`, `title`, `status`, `detail`, `path`,
`timestamp` and an optional `errors[]` array. Every endpoint except the public
actuator paths requires the `X-API-KEY` header.

### `POST /api/v1/flights/search`

Search for available flights. `preferredProvider` is **required** — the request
fails immediately if it is missing, unknown, or points to a disabled provider.

### `GET /api/v1/flights/providers`

List the providers currently enabled for flight search.

### `POST /api/v1/orders`

Create an order from a previously searched offer. `providerId` is required.

### `GET /api/v1/orders/{orderId}?providerId=AMADEUS`

Retrieve an order by id. `providerId` is **required**.

### `DELETE /api/v1/orders/{orderId}?providerId=AMADEUS`

Cancel an order. `providerId` is required.

### `GET /api/v1/orders/providers`

List the providers currently enabled for order management.

---

## 🧪 Testing

```bash
# Run every test in every module
mvn test

# Run only the domain tests (pure Java, no Spring context)
mvn -pl domain test

# Run only the presentation slice tests
mvn -pl presentation test
```

Test layout:

| Module          | Coverage focus                                                        |
|-----------------|-----------------------------------------------------------------------|
| `utilities/`    | Pure JUnit + AssertJ for the shared logging/string/date helpers       |
| `domain/`       | Pure JUnit + AssertJ. Validates invariants on entities & value objects |
| `application/`  | Router resolution logic, application service orchestration            |
| `infrastructure/` | Adapter mapping, mock-client behaviour                              |
| `presentation/` | `@WebMvcTest` slice tests for controllers, global exception handler and API-key security |
| `main/`         | `@SpringBootTest` context load (composition-root smoke test)           |

---

## 🏗️ Adding a new provider

> Full recipe lives in [`ARCHITECTURE.md` §5.3](ARCHITECTURE.md).

1. Add a flag to `application.yml`:
   ```yaml
   providers:
     sabre:
       enabled: true
       base-url: https://api.sabre.com
       ...
   ```
2. Create `SabreProperties` (`@ConfigurationProperties`).
3. Create `SabreClient` + three adapters implementing the three outbound ports
   in `domain/port/out/`, each annotated with
   `@ConditionalOnProperty(prefix = "providers.sabre", name = "enabled", havingValue = "true")`.
4. Done — `ProviderRouter` picks them up at boot, the REST API starts listing
   `"SABRE"` in `/providers`, and the rest of the system is unaware anything
   happened.

---

## 🧭 Project governance

- **Architecture document:** [`ARCHITECTURE.md`](ARCHITECTURE.md) is the
  single source of truth. If code drifts, fix the code, then update the
  document in the same commit.
- **Versioning:** this project follows [SemVer](https://semver.org/).
- **License:** Apache 2.0.

---

## 📜 License

Copyright © Core Service contributors.
Released under the [Apache License, Version 2.0](LICENSE).
