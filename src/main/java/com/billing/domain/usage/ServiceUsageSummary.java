package com.billing.domain.usage;

import com.billing.domain.common.UsageQuantity;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;

import java.util.List;

public record ServiceUsageSummary(
        String userId,
        ServiceType serviceType,
        UnitType unit,
        UsageQuantity totalQuantity,
        List<ResourceUsageSummary> resources
) {
}
