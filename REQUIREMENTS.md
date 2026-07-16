# Usage-Based Billing — Requirements

## Problem Statement

Design and implement a billing system that calculates charges for cloud-style resource usage across multiple users.

The system should:

- Track usage per resource per user.
- Apply service-specific pricing models.
- Generate invoices from accumulated billing data.

---

## Functional Requirements

### 1. Usage Tracking

The system must record usage events.

Each usage event must contain at least:

- User ID
- Resource ID
- Service Type
- Quantity
- Unit
- Timestamp

**Requirements:**

- Usage is tracked per **(User, Resource)** pair.
- A single user may consume multiple resources and multiple services.
- Usage events may arrive **out of order**.

### 2. Pricing Models

The system must support **at least** the following pricing models.

**Flat per unit** — e.g. Storage at $0.02 per GB-hour: `Total = Quantity × Rate`

**Tiered** — e.g. 150 compute hours: `100 × 0.10 + 50 × 0.08`

**Fixed subscription + overage** — e.g. $50 base, 1M included API calls, $0.001 overage:
`50 + 400,000 × 0.001` for 1.4M calls

**Extensibility:** Adding a new billing model must require a new strategy and configuration only — not changes to existing pricing logic.

### 3. Invoice Generation

Given a user and billing period **[start, end)**, generate an invoice with:

- Per-resource line items
- Per-service subtotals
- Grand total

Only usage inside the billing period is billed.

---

## Constraints

| Area | Rule |
|------|------|
| Money | Single fixed currency; no `float`/`double`; use precise decimal math |
| Pricing | External config (rates, tiers, services); engine must not hardcode prices |
| Events | Order-independent aggregation |
| Persistence | In-memory OK; must use a replaceable repository interface |
| Libraries | No external billing libraries; JSL for billing calculations |

---

## Deliverables

- Working billing system
- Driver/test with two users, three services, all pricing models, one invoice
- Clear separation: usage ingestion · pricing strategy · invoice assembly

---

## Expected Architecture

- Strategy pattern for pricing models
- Configuration-driven pricing
- Repository abstraction
- Immutable value objects for money and quantities
- Separation of concerns, SOLID, testable components
