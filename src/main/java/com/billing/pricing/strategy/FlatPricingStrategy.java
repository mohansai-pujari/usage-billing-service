package com.billing.pricing.strategy;

import com.billing.entity.common.Money;
import com.billing.entity.pricing.FlatPricingConfig;
import com.billing.entity.usage.ServiceUsageSummary;
import com.billing.enums.BillingType;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Pricing strategy for flat per-unit billing.
 */
@Component
public class FlatPricingStrategy extends AbstractPricingStrategy<FlatPricingConfig> {

    @Override
    public BillingType supportedBillingType() {
        return BillingType.FLAT;
    }

    @Override
    public Money calculate(ServiceUsageSummary usage, FlatPricingConfig pricingConfig) {
        Objects.requireNonNull(usage, "Usage summary cannot be null.");
        Objects.requireNonNull(pricingConfig, "Pricing configuration cannot be null.");

        return pricingConfig.getUnitPrice()
                .multiply(usage.getTotalQuantity().value());
    }
}