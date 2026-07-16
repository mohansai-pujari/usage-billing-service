package com.billing.support;

import com.billing.domain.common.ServiceKey;
import com.billing.domain.common.UnitKey;
import com.billing.domain.common.UsageQuantity;
import com.billing.domain.usage.UsageEvent;

import java.math.BigDecimal;
import java.time.Instant;

/** Shared test data builders. */
public final class TestEvents {

    private TestEvents() {
    }

    public static UsageEvent storage(String userId, String resourceId, String quantity, String timestamp) {
        return event(userId, resourceId, "storage", "GB_HOUR", quantity, timestamp);
    }

    public static UsageEvent compute(String userId, String resourceId, String quantity, String timestamp) {
        return event(userId, resourceId, "compute", "COMPUTE_HOUR", quantity, timestamp);
    }

    public static UsageEvent api(String userId, String resourceId, String quantity, String timestamp) {
        return event(userId, resourceId, "api", "API_CALL", quantity, timestamp);
    }

    public static UsageEvent event(
            String userId,
            String resourceId,
            String serviceType,
            String unit,
            String quantity,
            String timestamp) {
        return new UsageEvent(
                userId,
                resourceId,
                ServiceKey.of(serviceType),
                UnitKey.of(unit),
                UsageQuantity.of(new BigDecimal(quantity)),
                Instant.parse(timestamp));
    }
}
