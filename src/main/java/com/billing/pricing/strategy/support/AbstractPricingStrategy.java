package com.billing.pricing.strategy.support;

import com.billing.config.BillingProperties.PricingDefinition;
import com.billing.domain.common.Money;
import com.billing.domain.common.ServiceKey;
import com.billing.domain.common.UnitKey;
import com.billing.domain.enums.BillingType;
import com.billing.domain.pricing.PricingConfig;
import com.billing.domain.usage.ServiceUsageSummary;
import com.billing.pricing.strategy.PricingStrategy;

import java.math.BigDecimal;
import java.util.List;

/** Base class for service-level pricing with proportional line allocation by default. */
public abstract class AbstractPricingStrategy implements PricingStrategy {

    @Override
    public List<Money> allocateResourceLineAmounts(
            PricingConfig config,
            ServiceUsageSummary usage,
            Money serviceCharge) {
        return ProportionalLineAmountAllocator.allocate(
                serviceCharge,
                usage.resources(),
                usage.totalQuantity());
    }

    protected static PricingConfig config(
            ServiceKey serviceType,
            BillingType billingType,
            UnitKey unitType,
            BigDecimal unitPrice,
            List<PricingConfig.Tier> tiers,
            BigDecimal monthlyFee,
            long includedUnits,
            BigDecimal overageUnitPrice) {
        return new PricingConfig(
                serviceType,
                billingType,
                unitType,
                unitPrice,
                tiers,
                monthlyFee,
                includedUnits,
                overageUnitPrice);
    }
}
