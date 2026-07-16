package com.billing.pricing.strategy.support;

import com.billing.domain.common.Money;
import com.billing.domain.common.UsageQuantity;
import com.billing.domain.usage.ResourceUsageSummary;

import java.util.ArrayList;
import java.util.List;

/** Splits a service charge across resources proportionally by usage quantity. */
public final class ProportionalLineAmountAllocator {

    private ProportionalLineAmountAllocator() {
    }

    public static List<Money> allocate(
            Money serviceCharge,
            List<ResourceUsageSummary> resources,
            UsageQuantity totalQuantity) {
        return allocate(serviceCharge, resources, totalQuantity.asBigDecimal());
    }

    private static List<Money> allocate(
            Money serviceCharge,
            List<ResourceUsageSummary> resources,
            java.math.BigDecimal totalQuantity) {

        if (resources.isEmpty()) {
            return List.of();
        }
        if (resources.size() == 1) {
            return List.of(serviceCharge);
        }

        List<Money> lineAmounts = new ArrayList<>();
        Money allocated = Money.zero();

        for (int index = 0; index < resources.size(); index++) {
            ResourceUsageSummary resource = resources.get(index);
            if (index == resources.size() - 1) {
                lineAmounts.add(serviceCharge.subtract(allocated));
            } else {
                Money share = Money.proportionalShare(
                        serviceCharge,
                        resource.quantity().asBigDecimal(),
                        totalQuantity);
                lineAmounts.add(share);
                allocated = allocated.add(share);
            }
        }

        return List.copyOf(lineAmounts);
    }
}
