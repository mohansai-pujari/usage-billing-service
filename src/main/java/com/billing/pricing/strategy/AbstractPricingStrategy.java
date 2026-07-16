package com.billing.pricing.strategy;

import com.billing.entity.pricing.PricingConfig;

/**
 * Base class for all pricing strategies.
 */
public abstract class AbstractPricingStrategy<T extends PricingConfig>
        implements PricingStrategy<T> {
}