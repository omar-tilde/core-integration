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
- 🧱 **Hexagonal / clean architecture** — the `domain` is framework-free, the
  `application` layer orchestrates use cases, the `infrastructure` layer talks to
  the outside world, and the `presentation` layer is the only thing that
  exposes HTTP.
- 🛡️ **Provider-agnostic REST contract** — the API never leaks
  provider-specific fields; the `providerId` is the only traceability hook.
- ✅ **Battle-tested validation** — Jakarta Validation at the boundary,
  invariant enforcement in the domain layer.
- 🔐 **API-key authentication** — every application endpoint requires a valid
  API key; only a small public allow-list (`/actuator/health`,
  `/actuator/info`, `/error`) stays open.
- 🎯 **Explicit provider selection** — requests use only the provider they
  name and fail fast (404 / 503) when it is missing or disabled. There is no
  silent fallback to another provider.
- 🧰 **Shared util module** — logging, string and date helpers are
  available to every module.
- 📝 **Structured logging (Log4j2)** — every line can carry a `correlationId`
  through `LogUtils`, so a single request is traceable across modules.
- 🩺 **Spring Boot Actuator** — `health`, `info`, `metrics` out of the box.
- 🐳 **Production-grade container image** — multi-stage Dockerfile based on
  `eclipse-temurin:25`.

---

## 🧰 Tech stack

| Layer        | Technology                                                                 |
|--------------|----------------------------------------------------------------------------|
| Language     | **Java 25** (records, pattern matching)                                    |
| Framework    | **Spring Boot 4.0.7** + Spring Framework 7                                 |
| Web          | `spring-boot-starter-webmvc` (Tomcat 11)                                   |
| Reactive     | `spring-boot-starter-webflux` (WebClient, used by provider clients)         |
| Security     | `spring-boot-starter-security` (Spring Security 7)                         |
| Validation   | Jakarta Validation 3.1                                                     |
| Logging      | Apache **Log4j2** (via `spring-boot-starter-log4j2`)                       |
| JSON         | Jackson 3 (`tools.jackson.databind`)                                       |
| Build        | Maven 3.9+ (multi-module)                                                  |
| Test         | JUnit 5.11, AssertJ 3.26, Mockito 5.18                                     |
| Container    | `eclipse-temurin:25-jre`                                                  |

> **Maven coordinates.** `groupId` = **`com.core.service`**; `artifactId`s =
> **`main`**, **`application`**, **`domain`**, **`presentation`**,
> **`infrastructure`**, **`util`**. Java packages are **`com.core.service.*`**.

---

## 📁 Project structure

```text
core-integration/
├── util/          # Cross-cutting helpers: logging (Log4j2), string, date
├── domain/             # Pure Java: entities, value objects, enums, outbound ports
├── application/        # Use cases, commands, DTOs, mappers, router, services
├── infrastructure/     # Provider adapters (Travelport, Amadeus) + config
├── presentation/       # REST controllers, request/response, exception handler, HTTP security
└── main/               # @SpringBootApplication + application.yml (composition root)
```

The dependency graph points **inward** to `domain` (and `util`), and `main`
is the composition root that wires everything at runtime:

```text
                 main  (scans com.core.service, wires beans)
                  │
   ┌──────────────┼───────────────────────────────────┐
   ▼              ▼                                   ▼
presentation   application ─────▶ domain ◀──────── infrastructure
   │              │                      │                   │
   └────▶ util ◀─┴──────────────────────┴───────────────────┘
            (used by every module)
```

### Request flow

```text
Client ─▶ ApiKeyAuthenticationFilter ─▶ Controller ─▶ Application Service
                                                       │
                            ProviderRouter.resolve*(providerId)  (explicit, fail-fast)
                                                       │
                                            Provider Adapter (Travelport / Amadeus)
                                                       │
                                           Domain objects (invariants enforced)
                                                       │
                                 Response ◀── mapped DTOs  (errors → ErrorResponse JSON)
```

> 📘 For the deep dive, see [`ARCHITECTURE.md`](ARCHITECTURE.md).

---

## 🚀 Getting started

### Prerequisites

| Tool         | Version | Notes                                       |
|--------------|---------|---------------------------------------------|
| JDK          | **25**  | `java -version` must report 25+              |
| Maven        | **3.9+**| `mvn -version`                              |
| Docker       | 24+ (optional) | only required for the container workflow  |

### Build

```bash
mvn clean verify
```

Compiles every module, runs all unit tests, and produces the bootable JAR at
`main/target/core-integration-*.jar`.

### Run locally

```bash
# Set the API key the service will require on every protected endpoint
export API_KEY=dev-only-change-me

# From the JAR:
java -jar main/target/core-integration-*.jar

# Or directly from Maven:
mvn -pl main spring-boot:run
```

The service listens on **port 8080**. Health is public:

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
The most relevant knobs:

| Property                          | Default                                   | Purpose                                       |
|-----------------------------------|-------------------------------------------|-----------------------------------------------|
| `server.port`                     | `8080`                                    | HTTP listen port                              |
| `providers.travelport.enabled`    | `true`                                    | Toggle the Travelport client + adapters       |
| `providers.amadeus.enabled`       | `true`                                    | Toggle the Amadeus client + adapters          |
| `providers.travelport.base-url`   | `https://api.travelport.com/universal`    | Travelport endpoint                           |
| `providers.amadeus.base-url`      | `https://api.amadeus.com/v2`              | Amadeus endpoint                              |
| `api.key`                         | `${API_KEY:dev-only-change-me}`           | API key required on protected endpoints       |
| `api.header-name`                 | `${API_HEADER_NAME:X-API-KEY}`            | Request header that carries the API key       |
| `management.endpoints.web.exposure.include` | `health,info,metrics`           | Actuator endpoints exposed                    |

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

## 🔐 API-key authentication

Every application endpoint is protected by a shared API key:

- The key is sent in the `X-API-KEY` header (configurable via `api.header-name` /
  `API_HEADER_NAME`).
- The expected key is read from `api.key`, bound from the `API_KEY` environment
  variable (or `application.yml`). It is **never** hard-coded in Java.
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

## 📝 Logging (Log4j2)

The application logs through a **single Log4j2 backend**. `LogUtils` (in the
`util` module) is built directly on the Log4j2 API, and `main` uses
`spring-boot-starter-log4j2` (Logback is excluded) so SLF4J calls are bridged to
Log4j2.

`LogUtils.putCorrelationId(...)` / `clearCorrelationId()` store the correlation id
in Log4j2's `ThreadContext`, and the console pattern in
`util/src/main/resources/log4j2.xml` surfaces it on every line:

```text
%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%t] [%X{correlationId}] %logger{36} - %msg%n
```

Set a correlation id early in request handling to trace a single call across
modules.

---

## 🌐 HTTP API (v1)

All endpoints return JSON. Errors follow an [RFC 7807](https://datatracker.ietf.org/doc/html/rfc7807)-style
`ErrorResponse` (`type`, `title`, `status`, `detail`, `path`, `timestamp`, optional
`errors[]`). **Every endpoint except the public actuator paths requires the
`X-API-KEY` header.**

### `POST /api/v1/flights/search`

Search for available flights. `preferredProvider` is **required** — the request
fails immediately (400) if it is missing, unknown, or points to a disabled provider.

### `GET /api/v1/flights/providers`

List the provider ids currently enabled for flight search.

### `POST /api/v1/orders`

Create an order from a previously searched offer. `providerId` is **required**.

### `GET /api/v1/orders/{orderId}?providerId=AMADEUS`

Retrieve an order by id. `providerId` is **required**.

### `DELETE /api/v1/orders/{orderId}?providerId=AMADEUS`

Cancel an order. `providerId` is **required**.

### `GET /api/v1/orders/providers`

List the provider ids currently enabled for order management.

---

## 🧪 Testing

```bash
mvn test                 # every module
mvn -pl domain test      # a single module
mvn -pl presentation test # controllers + security slice
```

| Module          | Coverage focus                                                        |
|-----------------|-----------------------------------------------------------------------|
| `util/`    | Plain JUnit + AssertJ for the shared logging/string/date helpers       |
| `domain/`       | Plain JUnit + AssertJ. Validates invariants on entities & value objects |
| `application/`  | `ProviderRouter` resolution/fail-fast; service orchestration            |
| `infrastructure/` | Adapter mapping; mock-client behaviour                              |
| `presentation/` | `@WebMvcTest` (controllers) + `@SpringBootTest`/`@AutoConfigureMockMvc` (API-key security) |
| `main/`         | `@SpringBootTest` context load (composition-root smoke test)           |

---

## 🏗️ Adding a new provider

> Full recipe lives in [`ARCHITECTURE.md` §6.2](ARCHITECTURE.md).

1. Add a flag (and any other `providers.<id>.*` properties) to `application.yml`:
   ```yaml
   providers:
     sabre:
       enabled: true
       base-url: https://api.sabre.com
       ...
   ```
2. Create `SabreProperties` as a `@ConfigurationProperties(prefix = "providers.sabre")` record.
3. Create `SabreClient` (transport) and three adapters implementing the three
   outbound ports in `domain/port/out/`, each annotated with
   `@ConditionalOnProperty(prefix = "providers.sabre", name = "enabled", havingValue = "true")`.
4. Done — `main`'s component scan picks the beans up, `ProviderRouter` registers
   them, and the REST API starts listing `"SABRE"` in `/providers`.

---

## 🧭 Project governance

- **Architecture document:** [`ARCHITECTURE.md`](ARCHITECTURE.md) is the single
  source of truth. If code drifts, fix the code, then update the document in the
  same commit.
- **Versioning:** this project follows [SemVer](https://semver.org/).
- **License:** Apache 2.0.

---

## 📜 License

Copyright © Core Service contributors.
Released under the [Apache License, Version 2.0](LICENSE).
