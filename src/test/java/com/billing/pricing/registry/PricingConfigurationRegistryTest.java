package com.billing.pricing.registry;

import com.billing.config.BillingProperties;
import com.billing.support.DemoPricingProperties;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;
import com.billing.exception.ConfigurationException;
import com.billing.pricing.strategy.impl.FlatPricingStrategy;
import com.billing.pricing.strategy.impl.SubscriptionPricingStrategy;
import com.billing.pricing.strategy.impl.TieredPricingStrategy;
import com.billing.pricing.strategy.registry.PricingStrategyRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PricingConfigurationRegistryTest {

    @Test
    void requiresPricingForEveryServiceType() {
        BillingProperties properties = new BillingProperties();
        BillingProperties.PricingDefinition storage = new BillingProperties.PricingDefinition();
        storage.setBillingType("FLAT");
        storage.setUnit(UnitType.GB_HOUR);
        storage.setUnitPrice(new BigDecimal("0.02"));
        properties.getPricing().put(ServiceType.STORAGE, storage);

        PricingStrategyRegistry strategyRegistry = new PricingStrategyRegistry(List.of(
                new FlatPricingStrategy(),
                new TieredPricingStrategy(),
                new SubscriptionPricingStrategy()));

        assertThrows(ConfigurationException.class,
                () -> new PricingConfigurationRegistry(properties, strategyRegistry));
    }

    @Test
    void loadsConfiguredServicesFromProperties() {
        BillingProperties properties = DemoPricingProperties.create();
        PricingStrategyRegistry strategyRegistry = new PricingStrategyRegistry(List.of(
                new FlatPricingStrategy(),
                new TieredPricingStrategy(),
                new SubscriptionPricingStrategy()));
        PricingConfigurationRegistry registry = new PricingConfigurationRegistry(properties, strategyRegistry);

        assertEquals(new BigDecimal("0.02"), registry.getConfig(ServiceType.STORAGE).unitPrice());
        assertEquals(3, registry.getConfig(ServiceType.COMPUTE).tiers().size());
        assertEquals(new BigDecimal("50"), registry.getConfig(ServiceType.API).monthlyFee());
    }
}
