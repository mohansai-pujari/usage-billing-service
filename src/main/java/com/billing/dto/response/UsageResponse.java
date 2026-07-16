package com.billing.dto.response;

import com.billing.entity.usage.UsageEvent;

public record UsageResponse(
        String userId,
        String resourceId,
        String serviceType,
        String unitType,
        String quantity,
        String timestamp
) {
    public static UsageResponse from(UsageEvent event) {
        return new UsageResponse(
                event.getUserId().toString(),
                event.getResourceId().toString(),
                event.getServiceType().name(),
                event.getUnitType().name(),
                event.getQuantity().toString(),
                event.getTimestamp().toString()
        );
    }
}
