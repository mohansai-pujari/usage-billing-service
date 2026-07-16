package com.billing.pricing.strategy.registry;

import com.billing.domain.enums.BillingType;
import com.billing.exception.ConfigurationException;
import com.billing.pricing.strategy.PricingStrategy;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PricingStrategyRegistry {

    @Autowired
    private List<PricingStrategy> strategies;

    private Map<BillingType, PricingStrategy> strategyMap;

    public PricingStrategyRegistry() {
    }

    public PricingStrategyRegistry(List<PricingStrategy> strategies) {
        this.strategyMap = buildMap(strategies);
    }

    @PostConstruct
    void initialize() {
        this.strategyMap = buildMap(strategies);
    }

    public PricingStrategy get(BillingType billingType) {
        PricingStrategy strategy = strategyMap.get(billingType);
        if (strategy == null) {
            throw new ConfigurationException("No pricing strategy registered for billing type: " + billingType);
        }
        return strategy;
    }

    public boolean supports(BillingType billingType) {
        return strategyMap.containsKey(billingType);
    }

    private static Map<BillingType, PricingStrategy> buildMap(List<PricingStrategy> strategyList) {
        Map<BillingType, PricingStrategy> map = new EnumMap<>(BillingType.class);
        for (PricingStrategy strategy : strategyList) {
            if (map.containsKey(strategy.billingType())) {
                throw new ConfigurationException("Duplicate pricing strategy for billing type: " + strategy.billingType());
            }
            map.put(strategy.billingType(), strategy);
        }

        Map<BillingType, PricingStrategy> immutable = Map.copyOf(map);
        for (BillingType billingType : BillingType.values()) {
            if (!immutable.containsKey(billingType)) {
                throw new ConfigurationException("No pricing strategy registered for billing type: " + billingType);
            }
        }
        return immutable;
    }
}
