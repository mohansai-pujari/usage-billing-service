package com.billing.web.dto.response;

import com.billing.domain.usage.UsageEvent;

/** Outgoing usage record for {@code GET /usages}. */
public record UsageResponse(
        String userId,
        String resourceId,
        String serviceType,
        String unit,
        String quantity,
        String timestamp
) {
    public static UsageResponse from(UsageEvent event) {
        return new UsageResponse(
                event.userId(),
                event.resourceId(),
                event.serviceType().value(),
                event.unit().value(),
                event.quantity().asBigDecimal().toPlainString(),
                event.timestamp().toString());
    }
}
