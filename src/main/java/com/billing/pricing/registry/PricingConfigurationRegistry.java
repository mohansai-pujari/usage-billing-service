package com.billing.pricing.registry;

import com.billing.config.BillingProperties;
import com.billing.config.BillingProperties.PricingDefinition;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;
import com.billing.domain.pricing.PricingConfig;
import com.billing.exception.ConfigurationException;
import com.billing.exception.InvalidRequestException;
import com.billing.exception.ResourceNotFoundException;
import com.billing.exception.ServiceTypeUnitMismatchException;
import com.billing.pricing.strategy.registry.PricingStrategyRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class PricingConfigurationRegistry {

    private static final Logger log = LoggerFactory.getLogger(PricingConfigurationRegistry.class);

    @Autowired
    private BillingProperties billingProperties;

    @Autowired
    private PricingStrategyRegistry strategyRegistry;

    private Map<ServiceType, PricingConfig> pricingByService;

    public PricingConfigurationRegistry() {
    }

    public PricingConfigurationRegistry(
            BillingProperties billingProperties,
            PricingStrategyRegistry strategyRegistry) {
        this.pricingByService = load(billingProperties, strategyRegistry);
    }

    @PostConstruct
    void initialize() {
        this.pricingByService = load(billingProperties, strategyRegistry);
    }

    public PricingConfig getConfig(ServiceType serviceType) {
        if (serviceType == null) {
            throw new InvalidRequestException("Service type is required.");
        }
        PricingConfig config = pricingByService.get(serviceType);
        if (config == null) {
            throw new ResourceNotFoundException("Pricing configuration not found for service: " + serviceType);
        }
        return config;
    }

    public void validateUnit(ServiceType serviceType, UnitType unit) {
        if (!serviceType.accepts(unit)) {
            throw ServiceTypeUnitMismatchException.forPair(serviceType, unit);
        }
    }

    private static Map<ServiceType, PricingConfig> load(
            BillingProperties billingProperties,
            PricingStrategyRegistry strategyRegistry) {
        if (billingProperties.getPricing() == null || billingProperties.getPricing().isEmpty()) {
            throw new ConfigurationException("At least one pricing configuration is required.");
        }

        Map<ServiceType, PricingConfig> configs = new EnumMap<>(ServiceType.class);
        billingProperties.getPricing().forEach((serviceType, definition) -> {
            if (configs.containsKey(serviceType)) {
                throw new ConfigurationException("Duplicate pricing configuration for service: " + serviceType);
            }
            PricingDefinitionRules.validate(serviceType, definition);
            log.debug("Loading pricing configuration for service={}", serviceType);
            configs.put(serviceType, buildConfig(serviceType, definition, strategyRegistry));
        });

        for (ServiceType serviceType : ServiceType.values()) {
            if (!configs.containsKey(serviceType)) {
                throw new ConfigurationException("Missing pricing configuration for service: " + serviceType);
            }
        }

        log.info("Loaded pricing configuration for {} services", configs.size());
        return Map.copyOf(configs);
    }

    private static PricingConfig buildConfig(
            ServiceType serviceType,
            PricingDefinition definition,
            PricingStrategyRegistry strategyRegistry) {

        var billingType = PricingDefinitionRules.parseBillingType(definition, serviceType);
        UnitType unitType = PricingDefinitionRules.parseUnitType(definition, serviceType);

        if (!strategyRegistry.supports(billingType)) {
            throw new ConfigurationException("No pricing strategy registered for billing type: " + billingType);
        }

        return strategyRegistry.get(billingType).buildConfig(serviceType, unitType, definition);
    }
}
