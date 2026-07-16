# Usage Billing Service

## Overview
This service records usage events, aggregates them by resource and service, applies pricing rules, and produces invoices for a user over a billing period.

## Architecture and design choices
- The project follows a layered structure:
  - Controller layer for HTTP endpoints
  - Service layer for application orchestration
  - Repository layer for persistence abstraction
  - Pricing layer with strategy-based implementations
  - DTOs and entity objects for data transfer and domain modeling
- The design uses the Strategy pattern for pricing rules, allowing each billing model to be implemented independently.
- The Factory pattern selects the appropriate pricing strategy based on configuration.
- The service code depends on abstractions such as repositories and strategy interfaces, which improves testability and follows the Dependency Inversion Principle.
- The controller delegates usage handling to a dedicated usage service so that the HTTP layer does not directly manage business logic.

## Main flow
1. A client sends a usage event to the `/usage` endpoint.
2. The usage request is converted into a domain `UsageEvent`.
3. The event is stored through the repository.
4. When an invoice is requested for a user and time range, the billing service:
   - loads relevant usage records,
   - aggregates them by resource and service,
   - calculates charges using the configured pricing strategy,
   - assembles an invoice response.

## API endpoints
### Health
- `GET /health`
- Returns the service status.

### Record usage
- `POST /usage`
- Accepts a JSON body containing userId, resourceId, serviceType, unitType, quantity, and timestamp.
- Returns a success message.

### List usages
- `GET /usages`
- Query parameters:
  - `start` (required, epoch milliseconds)
  - `end` (required, epoch milliseconds)
  - `userId` (optional)
  - `serviceType` (optional)
- Returns usage entries matching the requested filters.

### Get invoice
- `GET /invoices/{userId}`
- Query parameters:
  - `start` (required, epoch milliseconds)
  - `end` (required, epoch milliseconds)
- Returns the invoice summary for the requested user and period.

## Example requests
### Record usage
```bash
curl -X POST http://localhost:8080/usage \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-1",
    "resourceId": "disk-1",
    "serviceType": "STORAGE",
    "unitType": "GB_HOUR",
    "quantity": 50,
    "timestamp": "2026-01-10T10:00:00Z"
  }'
```

### List usages
```bash
curl "http://localhost:8080/usages?start=1767225600000&end=1770000000000"
```

### Get invoice
```bash
curl "http://localhost:8080/invoices/user-1?start=1767225600000&end=1770000000000"
```

## Notes on SOLID and design patterns
- Single Responsibility: each component handles one clear responsibility such as usage recording, pricing, or invoice assembly.
- Open/Closed: new pricing strategies can be added without changing existing strategy consumers.
- Liskov Substitution: the strategy implementations share a common contract and are interchangeable.
- Interface Segregation: the usage and billing services expose focused operations rather than one oversized interface.
- Dependency Inversion: controllers and services depend on interfaces and abstractions instead of concrete implementations.
