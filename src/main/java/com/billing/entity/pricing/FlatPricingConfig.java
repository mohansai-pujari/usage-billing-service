package com.billing.entity.pricing;

import com.billing.entity.common.Money;
import com.billing.enums.BillingType;
import com.billing.enums.ServiceType;
import com.billing.enums.UnitType;
import com.billing.exception.InvalidRequestException;

import java.util.Objects;

/**
 * Configuration for flat per-unit pricing.
 *
 * Example:
 * Storage -> 0.02 USD per GB_HOUR
 */
public final class FlatPricingConfig extends PricingConfig {

    private final Money unitPrice;

    public FlatPricingConfig(ServiceType serviceType,
                             UnitType unitType,
                             Money unitPrice) {

        super(serviceType, BillingType.FLAT, unitType);

        this.unitPrice = Objects.requireNonNull(
                unitPrice,
                "Unit price cannot be null."
        );

        validate();
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    @Override
    protected void validate() {

        if (unitPrice.isNegative()) {
            throw new InvalidRequestException(
                    "Unit price cannot be negative."
            );
        }
    }

    @Override
    public String toString() {
        return "FlatPricingConfig{" +
                "serviceType=" + getServiceType() +
                ", unitType=" + getUnitType() +
                ", unitPrice=" + unitPrice +
                '}';
    }
}