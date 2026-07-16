package com.billing.pricing.strategy;

import com.billing.entity.common.Money;
import com.billing.entity.pricing.SubscriptionPricingConfig;
import com.billing.entity.usage.ServiceUsageSummary;
import com.billing.enums.BillingType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Pricing strategy for subscription-based billing.
 */
@Component
public class SubscriptionPricingStrategy extends AbstractPricingStrategy<SubscriptionPricingConfig> {

    @Override
    public BillingType supportedBillingType() {
        return BillingType.SUBSCRIPTION;
    }

    @Override
    public Money calculate(ServiceUsageSummary usage, SubscriptionPricingConfig pricingConfig) {
        Objects.requireNonNull(usage, "Usage summary cannot be null.");
        Objects.requireNonNull(pricingConfig, "Pricing configuration cannot be null.");

        BigDecimal totalUsage = usage.getTotalQuantity().value();
        BigDecimal includedUnits = BigDecimal.valueOf(pricingConfig.getIncludedUnits());
        BigDecimal overageUnits = totalUsage.subtract(includedUnits);

        if (overageUnits.signum() < 0) {
            overageUnits = BigDecimal.ZERO;
        }

        Money overageCharge = pricingConfig.getOverageUnitPrice()
                .multiply(overageUnits);

        return pricingConfig.getMonthlyFee()
                .add(overageCharge);
    }
}