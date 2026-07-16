package com.billing.entity.pricing;

import com.billing.enums.BillingType;
import com.billing.enums.ServiceType;
import com.billing.enums.UnitType;
import com.billing.exception.InvalidRequestException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Configuration for tiered pricing.
 */
public final class TieredPricingConfig extends PricingConfig {

    private final List<Tier> tiers;

    public TieredPricingConfig(ServiceType serviceType,
                               UnitType unitType,
                               List<Tier> tiers) {

        super(serviceType, BillingType.TIERED, unitType);

        Objects.requireNonNull(
                tiers,
                "Tier list cannot be null."
        );

        this.tiers = Collections.unmodifiableList(
                new ArrayList<>(tiers)
        );

        validate();
    }

    public List<Tier> getTiers() {
        return tiers;
    }

    @Override
    protected void validate() {

        if (tiers.isEmpty()) {
            throw new InvalidRequestException(
                    "At least one tier is required."
            );
        }

        long previousLimit = 0;

        for (int i = 0; i < tiers.size(); i++) {

            Tier tier = tiers.get(i);

            if (tier.isUnlimited()) {

                if (i != tiers.size() - 1) {
                    throw new InvalidRequestException(
                            "Unlimited tier must be the final tier."
                    );
                }

                break;
            }

            if (tier.getUpToUnits() <= previousLimit) {
                throw new InvalidRequestException(
                        "Tier limits must be strictly increasing."
                );
            }

            previousLimit = tier.getUpToUnits();
        }
    }

    @Override
    public String toString() {
        return "TieredPricingConfig{" +
                "serviceType=" + getServiceType() +
                ", unitType=" + getUnitType() +
                ", tiers=" + tiers +
                '}';
    }
}