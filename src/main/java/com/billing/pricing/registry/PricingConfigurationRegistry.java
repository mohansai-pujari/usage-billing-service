package com.billing.pricing.registry;

import com.billing.config.BillingProperties;
import com.billing.config.BillingProperties.PricingDefinition;
import com.billing.domain.common.ServiceKey;
import com.billing.domain.common.UnitKey;
import com.billing.domain.pricing.PricingConfig;
import com.billing.exception.ConfigurationException;
import com.billing.exception.InvalidRequestException;
import com.billing.exception.ResourceNotFoundException;
import com.billing.pricing.strategy.registry.PricingStrategyRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/** Loads and serves validated {@link PricingConfig} from external YAML configuration. */
public class PricingConfigurationRegistry {

    private static final Logger log = LoggerFactory.getLogger(PricingConfigurationRegistry.class);

    private final Map<ServiceKey, PricingConfig> pricingByService;

    public PricingConfigurationRegistry(
            BillingProperties billingProperties,
            PricingStrategyRegistry strategyRegistry) {
        if (billingProperties.getPricing() == null || billingProperties.getPricing().isEmpty()) {
            throw new ConfigurationException("At least one pricing configuration is required.");
        }

        Map<ServiceKey, PricingConfig> configs = new LinkedHashMap<>();
        billingProperties.getPricing().forEach((serviceType, definition) -> {
            ServiceKey serviceKey = PricingDefinitionParser.toServiceKey(serviceType);
            if (configs.containsKey(serviceKey)) {
                throw new ConfigurationException("Duplicate pricing configuration for service: " + serviceType);
            }
            log.debug("Loading pricing configuration for service={}", serviceKey.value());
            configs.put(serviceKey, buildConfig(serviceKey, definition, strategyRegistry));
        });

        this.pricingByService = Map.copyOf(configs);
        log.info("Loaded pricing configuration for {} services", pricingByService.size());
    }

    public PricingConfig getConfig(ServiceKey serviceType) {
        if (serviceType == null) {
            throw new InvalidRequestException("Service type is required.");
        }
        PricingConfig config = pricingByService.get(serviceType);
        if (config == null) {
            throw new ResourceNotFoundException("Pricing configuration not found for service: " + serviceType.value());
        }
        return config;
    }

    public void validateUnit(ServiceKey serviceType, UnitKey unit) {
        PricingConfig config = getConfig(serviceType);
        if (!config.unitType().equals(unit)) {
            throw new InvalidRequestException(
                    "Unit " + unit.value() + " is not valid for service " + serviceType.value()
                            + ". Expected " + config.unitType().value() + ".");
        }
    }

    private static PricingConfig buildConfig(
            ServiceKey serviceType,
            PricingDefinition definition,
            PricingStrategyRegistry strategyRegistry) {

        PricingDefinitionParser.requireDefinition(definition, serviceType.value());
        var billingType = PricingDefinitionParser.parseBillingType(definition, serviceType.value());
        UnitKey unitType = PricingDefinitionParser.parseUnitType(definition, serviceType.value());

        if (!strategyRegistry.supports(billingType)) {
            throw new ConfigurationException("No pricing strategy registered for billing type: " + billingType);
        }

        return strategyRegistry.get(billingType).buildConfig(serviceType, unitType, definition);
    }
}
