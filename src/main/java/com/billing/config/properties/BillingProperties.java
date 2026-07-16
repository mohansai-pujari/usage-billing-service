package com.billing.config.properties;

import com.billing.enums.ServiceType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.EnumMap;
import java.util.Map;

@ConfigurationProperties(prefix = "billing")
public class BillingProperties {

    private Map<ServiceType, PricingDefinitionProperties> pricing =
            new EnumMap<>(ServiceType.class);

    public Map<ServiceType, PricingDefinitionProperties> getPricing() {
        return pricing;
    }

    public void setPricing(
            Map<ServiceType, PricingDefinitionProperties> pricing) {
        this.pricing = pricing;
    }
}