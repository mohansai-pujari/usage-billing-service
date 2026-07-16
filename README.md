# Usage Billing Service

Spring Boot service that records cloud-style usage, applies YAML-driven pricing (flat, tiered, subscription), and generates invoices.

**Stack:** Java 17 · Spring Boot 3.5 · Maven · In-memory storage

Spec: [REQUIREMENTS.md](REQUIREMENTS.md) · Architecture: [ARCHITECTURE.md](ARCHITECTURE.md)

---

## Context

This service implements a **usage-based billing engine** for cloud-style resources: ingest usage per user and resource, price it with configurable models (flat, tiered, subscription), and produce invoices for a billing window `[start, end)`. It meets the [REQUIREMENTS.md](REQUIREMENTS.md) deliverable — two users, three services, invoice total **USD 466.00** — and exposes a REST API with Swagger for local testing.

---

## Contents

| | |
|---|---|
| [Quick start](#quick-start) | Commands, URLs, and how to run the demo |
| [API overview](#api) | Endpoints, request flow, billing period rules |
| → [GET /health](#get-health) | Health check |
| → [POST /usage](#post-usage) | Record a usage event |
| → [GET /usages](#get-usages) | List / paginate usage events |
| → [GET /invoices/{userId}](#get-invoicesuserid) | Invoice for one user |
| → [GET /invoices](#get-invoices) | Invoice across all users |
| → [POST /usage/bulk](#post-usagebulk) | Bulk ingest (local/dev only) |
| [Error handling](#error-handling) | HTTP status codes and client messages |
| [Structure](#structure) | Package layout and layers |
| [Design](#design) | Patterns and how to extend |
| [Tests](#tests) | Test suite overview |
| [REQUIREMENTS alignment](#requirements-alignment) | Spec checklist and intentional extensions |

---

## Quick start

```bash
mvn clean test
mvn spring-boot:run
mvn -q exec:java          # standalone demo (AssignmentDemo → USD 466.00)
```

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Actuator: http://localhost:8080/actuator/health · http://localhost:8080/actuator/metrics

---

## API

All timestamps are **epoch milliseconds (UTC)**. Billing periods use **`[start, end)`** — start inclusive, end exclusive.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/health` | Health check |
| POST | `/usage` | Record a usage event |
| GET | `/usages` | List usage events for a period (optional pagination) |
| GET | `/invoices/{userId}` | Invoice for one user |
| GET | `/invoices` | Invoice across all users (optional filters) |
| POST | `/usage/bulk` | Bulk ingest for local/dev testing only (see below) |

**Flow:** `POST /usage` → UsageService → BillingService → UsageRepository  
**Invoice:** BillingService → UsageRepository → InvoiceAssembler → UsageAggregator + BillingCalculator (strategies) → InvoiceCurrencyConverter

---

<a id="get-health"></a>

### `GET /health`

**Response `200`**

```json
{
  "status": "UP"
}
```

---

<a id="post-usage"></a>

### `POST /usage`

Record consumption for a user/resource. `serviceType` must be one of `STORAGE`, `COMPUTE`, or `API`. `unit` must match the service (`GB_HOUR`, `COMPUTE_HOUR`, `API_CALL`).

Optional `eventId` provides idempotency — duplicate IDs are accepted with `201` but not stored twice.

**Request**

```json
{
  "userId": "user-1",
  "resourceId": "disk-1",
  "serviceType": "STORAGE",
  "unit": "GB_HOUR",
  "quantity": 50,
  "timestamp": 1768039200000,
  "eventId": "evt-20260110-disk-1"
}
```

**Response `201`**

```json
{
  "message": "Usage recorded successfully"
}
```

---

<a id="get-usages"></a>

### `GET /usages`

List usage events in a billing period. Query params `userId` and `serviceType` are optional. Omit both `page` and `size` to return all matches; provide both to paginate (max page size 100).

**Example:** `GET /usages?userId=user-1&start=1767225600000&end=1769904000000&page=0&size=20`

**Response `200`**

```json
{
  "content": [
    {
      "userId": "user-1",
      "resourceId": "disk-1",
      "serviceType": "STORAGE",
      "unit": "GB_HOUR",
      "quantity": "50",
      "timestamp": 1768039200000
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

---

<a id="get-invoicesuserid"></a>

### `GET /invoices/{userId}`

Generate an invoice for one user. Optional query params: `currency` (default `USD`), `serviceType` (filter to one service).

**Example:** `GET /invoices/user-1?start=1767225600000&end=1769904000000`

After recording 50 GB-hours of storage (flat rate $0.02/GB-hour):

**Response `200`**

```json
{
  "userId": "user-1",
  "billingPeriodStart": 1767225600000,
  "billingPeriodEnd": 1769904000000,
  "currency": "USD",
  "lineItems": [
    {
      "resourceId": "disk-1",
      "description": "STORAGE usage",
      "quantity": "50",
      "unit": "GB_HOUR",
      "amount": "USD 1.00"
    }
  ],
  "serviceSubtotals": [
    {
      "serviceType": "STORAGE",
      "amount": "USD 1.00",
      "lineItems": [
        {
          "resourceId": "disk-1",
          "description": "STORAGE usage",
          "quantity": "50",
          "unit": "GB_HOUR",
          "amount": "USD 1.00"
        }
      ]
    }
  ],
  "total": "USD 1.00"
}
```

---

<a id="get-invoices"></a>

### `GET /invoices`

Same shape as above but across **all users** — `userId` in the response is `null`. Supports `start`, `end`, `currency`, `serviceType`.

**Example:** `GET /invoices?start=1767225600000&end=1769904000000&serviceType=STORAGE`

---

<a id="post-usagebulk"></a>

### `POST /usage/bulk` (local / dev only)

Internal testing helper. **Not registered** when the active Spring profile is outside `billing.test.bulk-upload.allowed-environments` (default: `local`, `dev`). No profile is treated as `local`.

- Empty body `{}` loads `classpath:test-data/usage-events.json` (17 events).
- Or send a `BulkUsageRequest` payload with an `events` array.

**Response `201`**

```json
{
  "message": "Bulk usage recorded successfully",
  "accepted": 17,
  "skippedDuplicates": 0,
  "source": "test-data/usage-events.json",
  "totalProcessed": 17
}
```

Configure allowed environments:

```yaml
billing:
  test:
    bulk-upload:
      allowed-environments:
        - local
        - dev
        # - staging   # add as needed
```

---

## Error handling

`ApiErrorHandler` maps every failure to a consistent `ErrorResponse` (`timestamp`, `status`, `error`, `message`, `path`). Internal details are logged only — never returned to clients.

| HTTP | Triggers |
|------|----------|
| **400** | Invalid or malformed JSON; Bean Validation on body; missing/invalid query params; **`serviceType` / `unit` mismatch**; invalid billing period or other bad input |
| **404** | No usage for invoice; resource/config not found; unknown or disabled route (e.g. bulk in prod) |
| **500** | Configuration failure; unexpected server error |

**400 messages (by case):**

| Case | Message |
|------|---------|
| Body validation (`@Valid`) | One or more request fields are invalid. |
| Malformed JSON / unknown enum | The request body is invalid or malformed. |
| Missing query param | Required request parameters are missing. |
| Invalid query param type | One or more request parameters are invalid. |
| Service type ↔ unit mismatch | Service type and unit do not match. |
| Other invalid input | The request could not be processed. Please verify your input and try again. |

**404 / 500:** `The requested resource could not be found.` · `The service is temporarily unavailable. Please try again later.` · `An unexpected error occurred. Please try again later.`

Covered in `BillingApiIntegrationTest` and `BillingServiceTest`.

---

## Structure

```
com.billing/
├── config/       BillingProperties, OpenApiConfig, BulkUploadEnvironmentCondition
├── application/  query objects (UsageQuery, InvoiceQuery, UsagePage)
├── domain/       Money, UsageQuantity, ServiceType, UnitType, UsageEvent, Invoice, …
├── storage/      UsageRepository → UsageStore
├── pricing/      UsageAggregator, BillingCalculator, InvoiceAssembler
│   ├── registry/     YAML → PricingConfig (startup validation)
│   ├── strategy/     Flat, Tiered, Subscription (+ registry)
│   └── currency/     InvoiceCurrencyConverter (optional multi-currency display)
├── service/      BillingService, UsageService
├── demo/         AssignmentDemo (no HTTP)
├── support/      BillingContextBuilder, BillingMetrics, demo fixtures
└── web/          controllers, DTOs, ApiErrorHandler
```

**Layers:** web → service → pricing / storage → domain (~64 Java source files)

Pricing components use `@Component` and are auto-discovered by Spring (`@Autowired` in services).

---

## Design

| Pattern | Where |
|---------|-------|
| Strategy | `PricingStrategy` + flat / tiered / subscription impls |
| Repository | `UsageRepository` / `UsageStore` |
| Value object | `Money`, `UsageQuantity`, `BillingPeriod`, … |
| Externalized config | `application.yml` → `BillingProperties` |
| Conditional bean | `BulkUsageController` via `@Conditional(BulkUploadEnvironmentCondition.class)` |
| DTO | `web/dto/*` at HTTP boundary |

**Extend:**

| Change | How |
|--------|-----|
| New billing model | `BillingType` enum + new `PricingStrategy` `@Component` |
| New service | Add `ServiceType` + `UnitType` enum values + YAML block under `billing.pricing` |
| New storage | Implement `UsageRepository` |
| Enable bulk in another env | Add profile name to `billing.test.bulk-upload.allowed-environments` |

---

## Tests

| File | Covers |
|------|--------|
| `AssignmentScenarioTest` | Exact spec pricing examples + deliverable (2 users, 3 services, **USD 466.00**) |
| `BillingServiceTest` | Models, line items, period rules, out-of-order events |
| `BillingApiIntegrationTest` | HTTP API, pagination, bulk ingest, eventId dedup, **error responses** |
| `BulkUploadProdIntegrationTest` | Bulk endpoint absent in `prod` profile |
| `PricingConfigurationRegistryTest` | YAML config loading |
| `PricingConfigurationValidationTest` | Startup validation rules |
| `InvoiceCurrencyConverterTest` | Multi-currency display |
| `UsageStoreTest` | Storage keys, dedup, query filtering |
| `BulkUploadEnvironmentConditionTest` | Environment gating logic |
| `AssignmentDemo` | Standalone driver (`mvn -q exec:java`) |

---

## REQUIREMENTS alignment

| Requirement | Status | Notes |
|-------------|--------|-------|
| Usage per (user, resource), out-of-order OK | ✅ | `UsageStore` + order-independent `UsageAggregator` |
| Event fields: user, resource, service, quantity, unit, timestamp | ✅ | `UsageEvent` / `UsageRequest` |
| Flat / tiered / subscription strategies | ✅ | YAML-driven via `PricingConfigurationRegistry` |
| Invoice: line items, subtotals, total, `[start,end)` | ✅ | `InvoiceAssembler` |
| BigDecimal money, no float/double | ✅ | `Money`, `UsageQuantity` |
| External pricing config (no hardcoded rates in engine) | ✅ | `application.yml` → strategies read config at runtime |
| Replaceable repository | ✅ | `UsageRepository` interface |
| Strategy extensibility (new billing model) | ✅ | New `PricingStrategy` + config only |
| Deliverable driver (2 users, 3 services, one invoice) | ✅ | `AssignmentDemo` + `AssignmentScenarioTest` → **USD 466.00** |
| Separation: ingestion · pricing · invoice assembly | ✅ | `UsageService` / `BillingService` · `pricing/*` · `InvoiceAssembler` |

### Intentional extensions (beyond spec)

| Feature | Purpose |
|---------|---------|
| Optional multi-currency display | `billing.currency.enabled` + `InvoiceCurrencyConverter`; disable for strict USD-only |
| Typed enums (`ServiceType`, `UnitType`) | Safer API + startup validation; new service still needs enum entry |
| `GET /usages` pagination | Operational convenience |
| Optional `eventId` dedup | Idempotent ingest |
| `POST /usage/bulk` | Local/dev Swagger testing only; beans not loaded in prod |
| Actuator metrics | `BillingMetrics` counters for usage saved / duplicates / invoices |
