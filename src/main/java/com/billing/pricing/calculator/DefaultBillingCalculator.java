package com.billing.pricing.calculator;

import com.billing.entity.common.Money;
import com.billing.entity.invoice.CalculatedCharge;
import com.billing.entity.pricing.PricingConfig;
import com.billing.entity.usage.ServiceUsageSummary;
import com.billing.pricing.factory.PricingStrategyFactory;
import com.billing.pricing.registry.PricingConfigurationRegistry;
import com.billing.pricing.strategy.PricingStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultBillingCalculator implements BillingCalculator {

    private final PricingConfigurationRegistry pricingRegistry;
    private final PricingStrategyFactory strategyFactory;

    public DefaultBillingCalculator(PricingConfigurationRegistry pricingRegistry, PricingStrategyFactory strategyFactory) {
        this.pricingRegistry = pricingRegistry;
        this.strategyFactory = strategyFactory;
    }

    @Override
    public List<CalculatedCharge> calculate(List<ServiceUsageSummary> summaries) {
        List<CalculatedCharge> charges = new ArrayList<>();

        for (ServiceUsageSummary summary : summaries) {
            PricingConfig config = pricingRegistry.getPricingConfig(summary.getServiceType());
            PricingStrategy<PricingConfig> strategy = strategyFactory.getStrategy(config);
            Money amount = strategy.calculate(summary, config);
            charges.add(new CalculatedCharge(summary, amount));
        }

        return List.copyOf(charges);
    }
}