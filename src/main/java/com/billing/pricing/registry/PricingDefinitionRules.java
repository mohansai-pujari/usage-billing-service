package com.billing.pricing.registry;

import com.billing.config.BillingProperties.PricingDefinition;
import com.billing.domain.enums.BillingType;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;
import com.billing.exception.ConfigurationException;

final class PricingDefinitionRules {

    private PricingDefinitionRules() {
    }

    static void validate(ServiceType serviceType, PricingDefinition definition) {
        requireDefinition(definition, serviceType);
        UnitType configuredUnit = parseUnitType(definition, serviceType);
        if (!serviceType.accepts(configuredUnit)) {
            throw new ConfigurationException(
                    "Unit " + configuredUnit + " is not valid for service " + serviceType
                            + ". Expected " + serviceType.expectedUnit() + ".");
        }
        validateBillingTypeFields(serviceType, parseBillingType(definition, serviceType), definition);
    }

    static BillingType parseBillingType(PricingDefinition definition, ServiceType serviceType) {
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

    static UnitType parseUnitType(PricingDefinition definition, ServiceType serviceType) {
        if (definition.getUnit() == null) {
            throw new ConfigurationException("Unit type is required for service: " + serviceType);
        }
        return definition.getUnit();
    }

    private static void requireDefinition(PricingDefinition definition, ServiceType serviceType) {
        if (definition == null) {
            throw new ConfigurationException("Pricing definition is missing for service: " + serviceType);
        }
    }

    private static void validateBillingTypeFields(
            ServiceType serviceType,
            BillingType billingType,
            PricingDefinition definition) {
        switch (billingType) {
            case FLAT -> {
                if (definition.getUnitPrice() == null) {
                    throw new ConfigurationException("Unit price is required for flat pricing on service: " + serviceType);
                }
            }
            case TIERED -> {
                if (definition.getTiers() == null || definition.getTiers().isEmpty()) {
                    throw new ConfigurationException("At least one tier is required for tiered pricing on service: " + serviceType);
                }
            }
            case SUBSCRIPTION -> {
                if (definition.getMonthlyFee() == null) {
                    throw new ConfigurationException("Monthly fee is required for subscription pricing on service: " + serviceType);
                }
                if (definition.getIncludedUnits() == null) {
                    throw new ConfigurationException("Included units are required for subscription pricing on service: " + serviceType);
                }
                if (definition.getOverageUnitPrice() == null) {
                    throw new ConfigurationException("Overage unit price is required for subscription pricing on service: " + serviceType);
                }
            }
        }
    }
}
