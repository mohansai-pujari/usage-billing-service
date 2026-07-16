package com.billing.entity.invoice;

import com.billing.entity.common.Money;
import com.billing.entity.usage.ServiceUsageSummary;

import java.util.Objects;

/**
 * Represents the calculated billing charge for a single service summary.
 */
public final class CalculatedCharge {

    private final ServiceUsageSummary usageSummary;
    private final Money amount;

    public CalculatedCharge(ServiceUsageSummary usageSummary, Money amount) {
        this.usageSummary = Objects.requireNonNull(usageSummary, "Usage summary cannot be null.");
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null.");
    }

    public ServiceUsageSummary getUsageSummary() {
        return usageSummary;
    }

    public Money getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "CalculatedCharge{" +
                "usageSummary=" + usageSummary +
                ", amount=" + amount +
                '}';
    }
}