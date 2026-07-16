package com.billing.entity.pricing;

import com.billing.entity.common.Money;
import com.billing.exception.InvalidRequestException;

import java.util.Objects;

/**
 * Represents a single pricing tier for tiered billing.
 *
 * Examples:
 *
 * Up to 100 units      -> USD 0.10 per unit
 * Up to 1000 units     -> USD 0.08 per unit
 * Above 1000 units     -> USD 0.05 per unit
 *
 * The last tier is represented by a null upper limit.
 */
public final class Tier {

    /**
     * Inclusive upper limit for this tier.
     * A null value represents an unlimited tier.
     */
    private final Long upToUnits;

    /**
     * Price charged per unit within this tier.
     */
    private final Money unitPrice;

    public Tier(Long upToUnits, Money unitPrice) {

        if (upToUnits != null && upToUnits <= 0) {
            throw new InvalidRequestException(
                    "Tier upper limit must be greater than zero."
            );
        }

        this.upToUnits = upToUnits;

        this.unitPrice = Objects.requireNonNull(
                unitPrice,
                "Unit price cannot be null."
        );

        if (unitPrice.isNegative()) {
            throw new InvalidRequestException(
                    "Unit price cannot be negative."
            );
        }
    }

    /**
     * Returns the inclusive upper limit of this tier.
     * Null indicates that the tier has no upper bound.
     */
    public Long getUpToUnits() {
        return upToUnits;
    }

    /**
     * Returns the price charged per unit.
     */
    public Money getUnitPrice() {
        return unitPrice;
    }

    /**
     * Returns true if this is the final (unlimited) tier.
     */
    public boolean isUnlimited() {
        return upToUnits == null;
    }

    @Override
    public String toString() {
        return "Tier{" +
                "upToUnits=" + upToUnits +
                ", unitPrice=" + unitPrice +
                '}';
    }
}