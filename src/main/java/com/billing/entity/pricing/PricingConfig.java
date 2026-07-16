package com.billing.entity.pricing;

import com.billing.enums.BillingType;
import com.billing.enums.ServiceType;
import com.billing.enums.UnitType;

import java.util.Objects;

/**
 * Base configuration for a billable service.
 *
 * Concrete pricing models extend this class and provide
 * their own pricing-specific configuration.
 */
public abstract class PricingConfig {

    private final ServiceType serviceType;

    private final BillingType billingType;

    private final UnitType unitType;

    protected PricingConfig(ServiceType serviceType,
                            BillingType billingType,
                            UnitType unitType) {

        this.serviceType = Objects.requireNonNull(
                serviceType,
                "Service type cannot be null.");

        this.billingType = Objects.requireNonNull(
                billingType,
                "Billing type cannot be null.");

        this.unitType = Objects.requireNonNull(
                unitType,
                "Unit type cannot be null.");
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public BillingType getBillingType() {
        return billingType;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    protected abstract void validate();

    @Override
    public String toString() {
        return "PricingConfig{" +
                "serviceType=" + serviceType +
                ", billingType=" + billingType +
                ", unitType=" + unitType +
                '}';
    }
}