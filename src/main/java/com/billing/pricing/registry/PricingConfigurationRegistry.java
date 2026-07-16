package com.billing.pricing.registry;

import com.billing.entity.pricing.PricingConfig;
import com.billing.enums.ServiceType;

import java.util.Map;

public interface PricingConfigurationRegistry {

    PricingConfig getPricingConfig(ServiceType serviceType);

    boolean contains(ServiceType serviceType);

    Map<ServiceType, PricingConfig> getAllConfigurations();

    int size();
}