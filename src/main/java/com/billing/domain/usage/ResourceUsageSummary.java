package com.billing.domain.usage;

import com.billing.domain.common.UsageQuantity;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;

public record ResourceUsageSummary(
        String userId,
        String resourceId,
        ServiceType serviceType,
        UnitType unit,
        UsageQuantity quantity
) {
}
