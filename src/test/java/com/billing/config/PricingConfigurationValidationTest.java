package com.billing.config;

import com.billing.config.BillingProperties.PricingDefinition;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;
import com.billing.exception.ConfigurationException;
import com.billing.pricing.registry.PricingConfigurationRegistry;
import com.billing.pricing.strategy.impl.FlatPricingStrategy;
import com.billing.pricing.strategy.impl.SubscriptionPricingStrategy;
import com.billing.pricing.strategy.impl.TieredPricingStrategy;
import com.billing.pricing.strategy.registry.PricingStrategyRegistry;
import com.billing.support.DemoPricingProperties;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PricingConfigurationValidationTest {

    @Test
    void acceptsValidDemoPricing() {
        assertDoesNotThrow(() -> registryFor(DemoPricingProperties.create()));
    }

    @Test
    void rejectsMismatchedServiceUnitMapping() {
        BillingProperties properties = DemoPricingProperties.create();
        properties.getPricing().get(ServiceType.STORAGE).setUnit(UnitType.API_CALL);
        assertThrows(ConfigurationException.class, () -> registryFor(properties));
    }

    @Test
    void rejectsMissingServicePricing() {
        BillingProperties properties = new BillingProperties();
        properties.setPricing(validPricingWithout(ServiceType.COMPUTE));
        assertThrows(ConfigurationException.class, () -> registryFor(properties));
    }

    private static PricingConfigurationRegistry registryFor(BillingProperties properties) {
        return new PricingConfigurationRegistry(properties, strategyRegistry());
    }

    private static PricingStrategyRegistry strategyRegistry() {
        return new PricingStrategyRegistry(List.of(
                new FlatPricingStrategy(),
                new TieredPricingStrategy(),
                new SubscriptionPricingStrategy()));
    }

    private static Map<ServiceType, PricingDefinition> validPricingWithout(ServiceType excluded) {
        Map<ServiceType, PricingDefinition> pricing = new EnumMap<>(DemoPricingProperties.create().getPricing());
        pricing.remove(excluded);
        return pricing;
    }
}
