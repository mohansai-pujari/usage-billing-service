package com.billing.domain.enums;

/**
 * Supported billing calculation models.
 * Each value maps to a {@link com.billing.pricing.strategy.PricingStrategy} implementation.
 */
public enum BillingType {
    FLAT,
    TIERED,
    SUBSCRIPTION
}
