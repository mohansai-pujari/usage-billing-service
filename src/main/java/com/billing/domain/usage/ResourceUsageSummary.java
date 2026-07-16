package com.billing.domain.usage;

import com.billing.domain.common.ServiceKey;
import com.billing.domain.common.UnitKey;
import com.billing.domain.common.UsageQuantity;

/** Aggregated usage for one resource within a service for a billing period. */
public record ResourceUsageSummary(
        String userId,
        String resourceId,
        ServiceKey serviceType,
        UnitKey unit,
        UsageQuantity quantity
) {
}
