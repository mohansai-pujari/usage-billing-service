package com.billing.entity.pricing;

import com.billing.entity.common.Money;
import com.billing.enums.BillingType;
import com.billing.enums.ServiceType;
import com.billing.enums.UnitType;
import com.billing.exception.InvalidRequestException;

import java.util.Objects;

/**
 * Configuration for subscription pricing.
 */
public final class SubscriptionPricingConfig extends PricingConfig {

    private final Money monthlyFee;

    private final long includedUnits;

    private final Money overageUnitPrice;

    public SubscriptionPricingConfig(ServiceType serviceType,
                                     UnitType unitType,
                                     Money monthlyFee,
                                     long includedUnits,
                                     Money overageUnitPrice) {

        super(serviceType, BillingType.SUBSCRIPTION, unitType);

        this.monthlyFee = Objects.requireNonNull(
                monthlyFee,
                "Monthly fee cannot be null."
        );

        this.overageUnitPrice = Objects.requireNonNull(
                overageUnitPrice,
                "Overage unit price cannot be null."
        );

        this.includedUnits = includedUnits;

        validate();
    }

    public Money getMonthlyFee() {
        return monthlyFee;
    }

    public long getIncludedUnits() {
        return includedUnits;
    }

    public Money getOverageUnitPrice() {
        return overageUnitPrice;
    }

    @Override
    protected void validate() {

        if (monthlyFee.isNegative()) {
            throw new InvalidRequestException(
                    "Monthly fee cannot be negative."
            );
        }

        if (includedUnits < 0) {
            throw new InvalidRequestException(
                    "Included units cannot be negative."
            );
        }

        if (overageUnitPrice.isNegative()) {
            throw new InvalidRequestException(
                    "Overage unit price cannot be negative."
            );
        }
    }

    @Override
    public String toString() {
        return "SubscriptionPricingConfig{" +
                "serviceType=" + getServiceType() +
                ", unitType=" + getUnitType() +
                ", monthlyFee=" + monthlyFee +
                ", includedUnits=" + includedUnits +
                ", overageUnitPrice=" + overageUnitPrice +
                '}';
    }
}