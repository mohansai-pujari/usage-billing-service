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

/** Subscription pricing: {@code monthlyFee + max(0, usage − included) × overageUnitPrice}. */
public class SubscriptionPricingStrategy extends AbstractPricingStrategy {

    @Override
    public BillingType billingType() {
        return BillingType.SUBSCRIPTION;
    }

    @Override
    public PricingConfig buildConfig(ServiceKey serviceType, UnitKey unitType, PricingDefinition definition) {
        if (definition.getMonthlyFee() == null) {
            throw new ConfigurationException("Monthly fee is required for subscription service: " + serviceType.value());
        }
        if (definition.getOverageUnitPrice() == null) {
            throw new ConfigurationException("Overage unit price is required for subscription service: " + serviceType.value());
        }
        if (Money.of(definition.getMonthlyFee()).isNegative()) {
            throw new ConfigurationException("Monthly fee cannot be negative for service: " + serviceType.value());
        }
        if (Money.of(definition.getOverageUnitPrice()).isNegative()) {
            throw new ConfigurationException("Overage unit price cannot be negative for service: " + serviceType.value());
        }
        if (definition.getIncludedUnits() != null && definition.getIncludedUnits() < 0) {
            throw new ConfigurationException("Included units cannot be negative for service: " + serviceType.value());
        }

        return config(
                serviceType,
                BillingType.SUBSCRIPTION,
                unitType,
                null,
                List.of(),
                definition.getMonthlyFee(),
                definition.getIncludedUnits() != null ? definition.getIncludedUnits() : 0,
                definition.getOverageUnitPrice());
    }

    @Override
    public Money calculate(PricingConfig config, BigDecimal totalUsage) {
        if (config.monthlyFee() == null) {
            throw new ConfigurationException("Monthly fee is missing for service: " + config.serviceType().value());
        }
        if (config.overageUnitPrice() == null) {
            throw new ConfigurationException("Overage unit price is missing for service: " + config.serviceType().value());
        }

        BigDecimal included = BigDecimal.valueOf(config.includedUnits());
        BigDecimal overage = totalUsage.subtract(included);
        if (overage.signum() < 0) {
            overage = BigDecimal.ZERO;
        }

        Money overageCharge = Money.of(config.overageUnitPrice()).multiply(overage);
        return Money.of(config.monthlyFee()).add(overageCharge);
    }
}
