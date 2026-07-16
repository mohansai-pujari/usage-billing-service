# Usage Billing Service

Spring Boot service that records cloud-style usage, applies YAML-driven pricing (flat, tiered, subscription), and generates invoices.

**Stack:** Java 17 · Spring Boot 3.5 · Maven · In-memory storage

Spec: [REQUIREMENTS.md](REQUIREMENTS.md) · Architecture: [ARCHITECTURE.md](ARCHITECTURE.md)

---

## Quick start

```bash
mvn clean test
mvn spring-boot:run
mvn -q exec:java          # standalone demo (AssignmentDemo)
```

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html

---

## API

| Method | Path | Description |
|--------|------|-------------|
| GET | `/health` | Health check |
| POST | `/usage` | Record usage event |
| GET | `/usages?start=&end=` | List usage (`userId`, `serviceType` optional) |
| GET | `/invoices/{userId}?start=&end=&currency=` | Generate invoice (`currency` defaults to `USD`) |

**Flow:** `POST /usage` → UsageService → BillingService → UsageRepository  
**Invoice:** BillingService → UsageAggregator → BillingCalculator (strategies) → InvoiceAssembler

---

## Structure

```
com.billing/
├── config/       BillingProperties, BillingBeanConfig, OpenApiConfig
├── domain/       Money, UsageQuantity, ServiceKey, UnitKey, UsageEvent, Invoice, …
├── storage/      UsageRepository → UsageStore
├── pricing/      UsageAggregator, BillingCalculator, InvoiceAssembler
│   ├── registry/     YAML → PricingConfig
│   ├── strategy/     Flat, Tiered, Subscription (+ registry)
│   └── currency/     InvoiceCurrencyConverter
├── service/      BillingService, UsageService
├── demo/         AssignmentDemo (no HTTP)
└── web/          controllers, DTOs, ApiErrorHandler
```

**Layers:** web → service → pricing / storage → domain

Pricing classes are Spring-free; wired via `BillingBeanConfig`.

---

## Design

| Pattern | Where |
|---------|-------|
| Strategy | `PricingStrategy` + flat / tiered / subscription impls |
| Repository | `UsageRepository` / `UsageStore` |
| Value object | `Money`, `UsageQuantity`, `BillingPeriod`, … |
| Externalized config | `application.yml` → `BillingProperties` |
| DTO | `web/dto/*` at HTTP boundary (domain types not exposed in JSON) |

**Extend:**

| Change | How |
|--------|-----|
| New billing model | `BillingType` enum + new `PricingStrategy` `@Bean` |
| New service | YAML block under `billing.pricing` |
| New storage | Implement `UsageRepository` |

---

## Tests

| File | Covers |
|------|--------|
| `AssignmentScenarioTest` | Exact spec pricing examples |
| `ConfigurableServiceBillingTest` | YAML-only new service end-to-end |
| `BillingServiceTest` | Models, line items, period rules |
| `BillingApiIntegrationTest` | HTTP API |
| `AssignmentDemo` | Standalone driver |
