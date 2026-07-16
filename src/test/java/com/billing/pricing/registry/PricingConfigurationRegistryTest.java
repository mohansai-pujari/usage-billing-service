package com.billing.pricing.registry;

import com.billing.config.BillingProperties;
import com.billing.domain.common.ServiceKey;
import com.billing.pricing.strategy.impl.FlatPricingStrategy;
import com.billing.pricing.strategy.impl.SubscriptionPricingStrategy;
import com.billing.pricing.strategy.impl.TieredPricingStrategy;
import com.billing.pricing.strategy.registry.PricingStrategyRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingConfigurationRegistryTest {

    @Test
    void loadsConfiguredServiceWithoutCodeChanges() {
        BillingProperties properties = new BillingProperties();
        BillingProperties.PricingDefinition archive = new BillingProperties.PricingDefinition();
        archive.setBillingType("FLAT");
        archive.setUnit("GB_HOUR");
        archive.setUnitPrice(new BigDecimal("0.03"));
        properties.getPricing().put("archive", archive);

        PricingStrategyRegistry strategyRegistry = new PricingStrategyRegistry(List.of(
                new FlatPricingStrategy(),
                new TieredPricingStrategy(),
                new SubscriptionPricingStrategy()));
        PricingConfigurationRegistry registry = new PricingConfigurationRegistry(properties, strategyRegistry);

        assertEquals(new BigDecimal("0.03"), registry.getConfig(ServiceKey.of("archive")).unitPrice());
    }
}
