package com.billing.pricing;

import com.billing.domain.common.Money;
import com.billing.domain.pricing.PricingConfig;
import com.billing.domain.usage.ServiceUsageSummary;
import com.billing.exception.InvalidRequestException;
import com.billing.pricing.strategy.PricingStrategy;
import com.billing.pricing.strategy.registry.PricingStrategyRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BillingCalculator {

    @Autowired
    private PricingStrategyRegistry strategyRegistry;

    public BillingCalculator() {
    }

    public BillingCalculator(PricingStrategyRegistry strategyRegistry) {
        this.strategyRegistry = strategyRegistry;
    }

    public Money calculateServiceCharge(PricingConfig config, ServiceUsageSummary usage) {
        if (usage == null) {
            throw new InvalidRequestException("Service usage summary cannot be null.");
        }
        if (usage.totalQuantity() == null || usage.totalQuantity().asBigDecimal().signum() < 0) {
            throw new InvalidRequestException("Service usage quantity cannot be negative.");
        }
        return strategy(config).calculate(config, usage.totalQuantity().asBigDecimal());
    }

    public List<Money> calculateResourceLineAmounts(
            PricingConfig config,
            ServiceUsageSummary usage,
            Money serviceCharge) {
        return strategy(config).allocateResourceLineAmounts(config, usage, serviceCharge);
    }

    private PricingStrategy strategy(PricingConfig config) {
        return strategyRegistry.get(config.billingType());
    }
}
