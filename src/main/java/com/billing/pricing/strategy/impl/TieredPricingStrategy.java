package com.billing.pricing.strategy.impl;

import com.billing.config.BillingProperties.PricingDefinition;
import com.billing.domain.common.Money;
import com.billing.domain.common.ServiceKey;
import com.billing.domain.common.UnitKey;
import com.billing.domain.enums.BillingType;
import com.billing.domain.pricing.PricingConfig;
import com.billing.exception.ConfigurationException;
import com.billing.pricing.strategy.support.AbstractPricingStrategy;

import java.math.BigDecimal;
import java.util.List;

/** Tiered pricing: bills usage across ordered tier slabs at service level. */
public class TieredPricingStrategy extends AbstractPricingStrategy {

    @Override
    public BillingType billingType() {
        return BillingType.TIERED;
    }

    @Override
    public PricingConfig buildConfig(ServiceKey serviceType, UnitKey unitType, PricingDefinition definition) {
        if (definition.getTiers() == null || definition.getTiers().isEmpty()) {
            throw new ConfigurationException("At least one tier is required for service: " + serviceType.value());
        }

        List<PricingConfig.Tier> tiers = definition.getTiers().stream()
                .map(t -> new PricingConfig.Tier(t.getUpTo(), t.getUnitPrice()))
                .toList();

        return config(serviceType, BillingType.TIERED, unitType, null, tiers, null, 0, null);
    }

    @Override
    public Money calculate(PricingConfig config, BigDecimal totalUsage) {
        if (config.tiers() == null || config.tiers().isEmpty()) {
            throw new ConfigurationException("Tier configuration is missing for service: " + config.serviceType().value());
        }

        Money totalCharge = Money.zero();
        BigDecimal remaining = totalUsage;
        long previousLimit = 0;

        for (PricingConfig.Tier tier : config.tiers()) {
            if (remaining.signum() <= 0) {
                break;
            }

            BigDecimal billableUnits;
            if (tier.unlimited()) {
                billableUnits = remaining;
            } else {
                long capacity = tier.upTo() - previousLimit;
                billableUnits = remaining.min(BigDecimal.valueOf(capacity));
                previousLimit = tier.upTo();
            }

            totalCharge = totalCharge.add(Money.of(tier.unitPrice()).multiply(billableUnits));
            remaining = remaining.subtract(billableUnits);
        }

        return totalCharge;
    }
}
