package com.billing.domain.usage;

import com.billing.domain.common.ServiceKey;
import com.billing.domain.common.UnitKey;
import com.billing.domain.common.UsageQuantity;

import java.util.List;

/** Aggregated usage for one service across all resources for a user in a billing period. */
public record ServiceUsageSummary(
        String userId,
        ServiceKey serviceType,
        UnitKey unit,
        UsageQuantity totalQuantity,
        List<ResourceUsageSummary> resources
) {
}
