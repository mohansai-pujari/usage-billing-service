package com.billing.enums;

/**
 * Represents the billing model used by a service.
 */
public enum BillingType {

    /**
     * Fixed rate per unit consumed.
     */
    FLAT,

    /**
     * Pricing changes based on consumption slabs.
     */
    TIERED,

    /**
     * Fixed monthly subscription with included usage
     * and overage charges beyond the included limit.
     */
    SUBSCRIPTION
}