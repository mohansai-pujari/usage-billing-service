package com.billing.pricing.loader;

import com.billing.config.properties.BillingProperties;
import com.billing.config.properties.PricingDefinitionProperties;
import com.billing.entity.pricing.PricingConfig;
import com.billing.enums.ServiceType;
import com.billing.pricing.mapper.PricingConfigMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class PricingConfigurationLoader {

    private final BillingProperties billingProperties;
    private final PricingConfigMapper pricingConfigMapper;

    public PricingConfigurationLoader(BillingProperties billingProperties, PricingConfigMapper pricingConfigMapper) {
        this.billingProperties = billingProperties;
        this.pricingConfigMapper = pricingConfigMapper;
    }

    @Bean
    public List<PricingConfig> pricingConfigurations() {
        return billingProperties.getPricing()
                .entrySet()
                .stream()
                .map(this::mapEntry)
                .toList(); // Java 17
    }

    private PricingConfig mapEntry(Map.Entry<ServiceType, PricingDefinitionProperties> entry) {
        return pricingConfigMapper.map(entry.getKey(), entry.getValue());
    }
}