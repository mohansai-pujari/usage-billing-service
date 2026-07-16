# Interview Screening Question: Usage-Based Billing System

## Problem Statement

Design and implement a billing system that calculates charges for cloud-style resource usage across multiple users.

The system should:

- Track usage per resource per user.
- Apply service-specific pricing models.
- Generate invoices from accumulated billing data.

---

# Functional Requirements

## 1. Usage Tracking

The system must record usage events.

Each usage event must contain at least:

- User ID
- Resource ID
- Service Type
- Quantity
- Unit
- Timestamp

### Requirements

- Usage is tracked per **(User, Resource)** pair.
- A single user may consume:
    - Multiple resources
    - Multiple services
- Usage events may arrive **out of order**.

---

## 2. Pricing Models (Billing Types)

Different services use different billing strategies.

The system must support **at least** the following pricing models.

### Flat Per Unit

Example:

- Storage
- Price = **$0.02 per GB-hour**

Formula:

```
Total Charge = Quantity × Rate
```

---

### Tiered Pricing

Example:

| Usage Range | Rate |
|-------------|------|
| First 100 hours | $0.10 |
| Next 900 hours | $0.08 |
| Beyond 1000 | $0.05 |

Example:

```
150 Compute Hours

100 × 0.10
50 × 0.08
```

---

### Fixed Subscription + Overage

Example:

Monthly Subscription

- Base Price = $50
- Included API Calls = 1,000,000
- Overage = $0.001 per call

Example:

```
Usage = 1,400,000

Base = $50

Overage

400,000 × 0.001
```

---

## Extensibility Requirement

Adding a **new billing model** must require:

- Creating a new strategy
- Adding new configuration

It **must NOT** require modifying existing pricing logic.

---

# Invoice Generation

Given:

- User
- Billing Period **[start, end)**

Generate an invoice containing:

- Per-resource line items
- Per-service subtotals
- Grand total

### Billing Rules

- Only usage events inside the billing period are considered.
- Events outside the period must not be billed.

---

# Constraints & Specifications

## 1. Money Representation

- Use a single fixed currency.
- Do **not** use binary floating-point (`float` / `double`) for money.
- Monetary calculations must be precise.

---

## 2. Configuration Driven Pricing

Pricing configuration must be external.

Examples:

- Flat rates
- Tier definitions
- Subscription prices

must come from configuration/data.

Changing:

- Prices
- Tiers
- Services

should **not** require modifying the billing engine.

---

## 3. Out-of-Order Usage Events

Usage events may arrive in any order.

The implementation **must not** depend on insertion order.

---

## 4. Persistence

Persistence is **out of scope**.

An in-memory repository is acceptable.

However:

- Repository should be hidden behind an interface.
- Storage implementation should be replaceable.

---

## 5. Libraries

- No external billing libraries.
- Use only the Java Standard Library for billing logic.

---

# Deliverables

The implementation should include:

- A working billing system.
- A small driver/test demonstrating:
    - Two users
    - At least three services
    - All three pricing models
    - One generated invoice

The implementation must clearly separate:

- Usage ingestion
- Pricing strategy layer
- Invoice assembly

---

# Evaluation Criteria

## 1. Extensibility

Adding a new billing model should require:

- New Strategy
- New Configuration

No changes to existing pricing code.

Example interview probe:

> Add a "Graduated with Cap" pricing model.

---

## 2. Modularity

Pricing logic should be independent of:

- Usage storage
- Invoice formatting

---

## 3. Configuration Driven Design

Pricing information should be resolved from configuration at runtime.

No hardcoded pricing inside the calculation engine.

---

## 4. Correctness

The implementation will be evaluated on:

- Billing period handling
- Tier calculations
- Money calculations
- Invoice totals

---

## 5. Readability

The implementation should emphasize:

- Clear class boundaries
- Good naming
- Maintainable code
- Simplicity over cleverness

---

# Expected Architecture Characteristics

A strong solution should demonstrate:

- Strategy Pattern for pricing models
- Configuration-driven pricing
- Repository abstraction
- Immutable value objects for money and quantities
- Separation of concerns
- SOLID principles
- Easily extensible billing engine
- Testable components