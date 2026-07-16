package com.billing.pricing.strategy;

import com.billing.entity.common.Money;
import com.billing.entity.pricing.TieredPricingConfig;
import com.billing.entity.usage.ServiceUsageSummary;
import com.billing.enums.BillingType;
import com.billing.pricing.calculator.TierCalculator;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Pricing strategy for tiered billing.
 */
@Component
public class TieredPricingStrategy extends AbstractPricingStrategy<TieredPricingConfig> {

    private final TierCalculator tierCalculator;

    public TieredPricingStrategy(TierCalculator tierCalculator) {
        this.tierCalculator = tierCalculator;
    }

    @Override
    public BillingType supportedBillingType() {
        return BillingType.TIERED;
    }

    @Override
    public Money calculate(ServiceUsageSummary usage, TieredPricingConfig pricingConfig) {
        Objects.requireNonNull(usage, "Usage summary cannot be null.");
        Objects.requireNonNull(pricingConfig, "Pricing configuration cannot be null.");

        return tierCalculator.calculate(usage.getTotalQuantity().value(), pricingConfig);
    }
}