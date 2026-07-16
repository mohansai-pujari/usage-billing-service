package com.billing.pricing.factory;

import com.billing.entity.pricing.PricingConfig;
import com.billing.enums.BillingType;
import com.billing.exception.InvalidRequestException;
import com.billing.pricing.strategy.PricingStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Resolves the correct pricing strategy for a billing type.
 */
@Component
public class PricingStrategyFactory {

    private final Map<BillingType, PricingStrategy<?>> strategyMap;

    public PricingStrategyFactory(List<PricingStrategy<?>> strategies) {
        Objects.requireNonNull(strategies, "Strategies cannot be null.");

        this.strategyMap = new EnumMap<>(BillingType.class);

        for (PricingStrategy<?> strategy : strategies) {
            BillingType billingType = strategy.supportedBillingType();
            if (strategyMap.containsKey(billingType)) {
                throw new IllegalStateException("Duplicate strategy registered for " + billingType);
            }
            strategyMap.put(billingType, strategy);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends PricingConfig> PricingStrategy<T> getStrategy(T pricingConfig) {
        Objects.requireNonNull(pricingConfig, "Pricing config cannot be null.");

        PricingStrategy<?> strategy = strategyMap.get(pricingConfig.getBillingType());
        if (strategy == null) {
            throw new InvalidRequestException("No strategy registered for billing type: " + pricingConfig.getBillingType());
        }

        return (PricingStrategy<T>) strategy;
    }
}