package com.billing.pricing.registry;

import com.billing.config.BillingProperties.PricingDefinition;
import com.billing.domain.common.ServiceKey;
import com.billing.domain.common.UnitKey;
import com.billing.domain.enums.BillingType;
import com.billing.exception.ConfigurationException;

/** Parses shared pricing-definition fields from YAML before strategy-specific config building. */
final class PricingDefinitionParser {

    private PricingDefinitionParser() {
    }

    static void requireDefinition(PricingDefinition definition, String serviceType) {
        if (definition == null) {
            throw new ConfigurationException("Pricing definition is missing for service: " + serviceType);
        }
    }

    static BillingType parseBillingType(PricingDefinition definition, String serviceType) {
        if (definition.getBillingType() == null || definition.getBillingType().isBlank()) {
            throw new ConfigurationException("Billing type is required for service: " + serviceType);
        }
        try {
            return BillingType.valueOf(definition.getBillingType().trim());
        } catch (IllegalArgumentException ex) {
            throw new ConfigurationException(
                    "Invalid billing type for service " + serviceType + ": " + definition.getBillingType(),
                    ex);
        }
    }

    static UnitKey parseUnitType(PricingDefinition definition, String serviceType) {
        if (definition.getUnit() == null || definition.getUnit().isBlank()) {
            throw new ConfigurationException("Unit type is required for service: " + serviceType);
        }
        return UnitKey.of(definition.getUnit());
    }

    static ServiceKey toServiceKey(String serviceType) {
        return ServiceKey.of(serviceType);
    }
}
