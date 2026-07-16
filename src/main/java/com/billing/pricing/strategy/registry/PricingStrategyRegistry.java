package com.billing.pricing.strategy.registry;

import com.billing.domain.enums.BillingType;
import com.billing.exception.ConfigurationException;
import com.billing.pricing.strategy.PricingStrategy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Registry of {@link PricingStrategy} implementations keyed by {@link BillingType}. */
public class PricingStrategyRegistry {

    private final Map<BillingType, PricingStrategy> strategies;

    public PricingStrategyRegistry(List<PricingStrategy> strategyList) {
        Map<BillingType, PricingStrategy> map = new EnumMap<>(BillingType.class);
        for (PricingStrategy strategy : strategyList) {
            if (map.containsKey(strategy.billingType())) {
                throw new ConfigurationException("Duplicate pricing strategy for billing type: " + strategy.billingType());
            }
            map.put(strategy.billingType(), strategy);
        }
        this.strategies = Map.copyOf(map);
        for (BillingType billingType : BillingType.values()) {
            if (!strategies.containsKey(billingType)) {
                throw new ConfigurationException(
                        "No pricing strategy registered for billing type: " + billingType);
            }
        }
    }

    public PricingStrategy get(BillingType billingType) {
        PricingStrategy strategy = strategies.get(billingType);
        if (strategy == null) {
            throw new ConfigurationException("No pricing strategy registered for billing type: " + billingType);
        }
        return strategy;
    }

    public boolean supports(BillingType billingType) {
        return strategies.containsKey(billingType);
    }
}
