package com.billing.pricing.calculator;

import com.billing.entity.common.Money;
import com.billing.entity.pricing.Tier;
import com.billing.entity.pricing.TieredPricingConfig;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Performs tiered pricing calculations.
 * <p>
 * This class is responsible only for the mathematics of
 * tier-based billing.
 * <p>
 * It does not know anything about users, invoices,
 * repositories or billing periods.
 */
@Component
public class TierCalculator {

    /**
     * Calculates the total charge for the supplied usage.
     * <p>
     * Example:
     * <p>
     * Usage = 1200
     * <p>
     * Tier1 : Up to 100 @ 0.10
     * Tier2 : Up to 1000 @ 0.08
     * Tier3 : Unlimited @ 0.05
     */
    public Money calculate(BigDecimal totalUsage, TieredPricingConfig pricingConfig) {

        Objects.requireNonNull(totalUsage, "Total usage cannot be null.");

        Objects.requireNonNull(pricingConfig, "Pricing configuration cannot be null.");

        if (totalUsage.signum() < 0) {
            throw new IllegalArgumentException("Usage cannot be negative.");
        }

        Money totalCharge = Money.zero();

        BigDecimal remainingUsage = totalUsage;
        long previousTierLimit = 0;

        for (Tier tier : pricingConfig.getTiers()) {

            if (remainingUsage.signum() <= 0) {
                break;
            }

            BigDecimal billableUnits;

            if (tier.isUnlimited()) {
                billableUnits = remainingUsage;
            } else {
                long currentTierCapacity = tier.getUpToUnits() - previousTierLimit;
                billableUnits = remainingUsage.min(BigDecimal.valueOf(currentTierCapacity));
                previousTierLimit = tier.getUpToUnits();
            }

            Money tierCharge = tier.getUnitPrice()
                    .multiply(billableUnits);

            totalCharge = totalCharge.add(tierCharge);

            remainingUsage = remainingUsage.subtract(billableUnits);
        }

        return totalCharge;
    }
}