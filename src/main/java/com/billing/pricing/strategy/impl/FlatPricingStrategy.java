package com.billing.pricing.strategy.impl;

import com.billing.config.BillingProperties.PricingDefinition;
import com.billing.domain.common.Money;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;
import com.billing.domain.enums.BillingType;
import com.billing.domain.pricing.PricingConfig;
import com.billing.domain.usage.ServiceUsageSummary;
import com.billing.exception.ConfigurationException;
import com.billing.pricing.strategy.support.AbstractPricingStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class FlatPricingStrategy extends AbstractPricingStrategy {

    @Override
    public BillingType billingType() {
        return BillingType.FLAT;
    }

    @Override
    public PricingConfig buildConfig(ServiceType serviceType, UnitType unitType, PricingDefinition definition) {
        if (definition.getUnitPrice() == null) {
            throw new ConfigurationException("Unit price is required for flat pricing on service: " + serviceType);
        }
        if (Money.of(definition.getUnitPrice()).isNegative()) {
            throw new ConfigurationException("Unit price cannot be negative for service: " + serviceType);
        }

        return config(serviceType, BillingType.FLAT, unitType, definition.getUnitPrice(), List.of(), null, 0, null);
    }

    @Override
    public Money calculate(PricingConfig config, BigDecimal quantity) {
        if (config.unitPrice() == null) {
            throw new ConfigurationException("Unit price is missing for flat pricing on service: " + config.serviceType());
        }
        return Money.of(config.unitPrice()).multiply(quantity);
    }

    @Override
    public List<Money> allocateResourceLineAmounts(
            PricingConfig config,
            ServiceUsageSummary usage,
            Money serviceCharge) {
        return usage.resources().stream()
                .map(resource -> calculate(config, resource.quantity().asBigDecimal()))
                .toList();
    }
}
