package com.billing.domain.pricing;

import com.billing.domain.common.Money;
import com.billing.domain.enums.BillingType;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;
import com.billing.exception.ConfigurationException;

import java.math.BigDecimal;
import java.util.List;

public record PricingConfig(
        ServiceType serviceType,
        BillingType billingType,
        UnitType unitType,
        BigDecimal unitPrice,
        List<Tier> tiers,
        BigDecimal monthlyFee,
        long includedUnits,
        BigDecimal overageUnitPrice
) {
    public record Tier(Long upTo, BigDecimal unitPrice) {
        public Tier {
            if (upTo != null && upTo <= 0) {
                throw new ConfigurationException("Tier upper limit must be greater than zero.");
            }
            if (unitPrice == null) {
                throw new ConfigurationException("Tier unit price is required.");
            }
            if (Money.of(unitPrice).isNegative()) {
                throw new ConfigurationException("Tier unit price cannot be negative.");
            }
        }

        public boolean unlimited() {
            return upTo == null;
        }
    }
}
