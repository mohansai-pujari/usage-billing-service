package com.billing.pricing.registry;

import com.billing.entity.pricing.PricingConfig;
import com.billing.enums.ServiceType;
import com.billing.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable in-memory pricing configuration registry.
 */
@Component
public class InMemoryPricingConfigurationRegistry implements PricingConfigurationRegistry {

    private final Map<ServiceType, PricingConfig> pricingConfigurations;

    public InMemoryPricingConfigurationRegistry(List<PricingConfig> pricingConfigs) {

        Objects.requireNonNull(pricingConfigs, "Pricing configuration list cannot be null.");

        EnumMap<ServiceType, PricingConfig> configurations = new EnumMap<>(ServiceType.class);

        for (PricingConfig config : pricingConfigs) {

            ServiceType serviceType = config.getServiceType();

            if (configurations.containsKey(serviceType)) {
                throw new IllegalStateException("Duplicate pricing configuration found for service: " + serviceType);
            }

            configurations.put(serviceType, config);
        }

        this.pricingConfigurations = Collections.unmodifiableMap(configurations);
    }

    @Override
    public PricingConfig getPricingConfig(ServiceType serviceType) {

        Objects.requireNonNull(serviceType);

        PricingConfig config = pricingConfigurations.get(serviceType);

        if (config == null) {
            throw new ResourceNotFoundException("Pricing configuration not found for service: " + serviceType);
        }

        return config;
    }

    @Override
    public boolean contains(ServiceType serviceType) {
        return pricingConfigurations.containsKey(serviceType);
    }

    @Override
    public Map<ServiceType, PricingConfig> getAllConfigurations() {
        return pricingConfigurations;
    }

    @Override
    public int size() {
        return pricingConfigurations.size();
    }
}