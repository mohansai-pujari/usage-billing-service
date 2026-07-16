package com.billing.support;

import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;
import com.billing.domain.usage.UsageEvent;

import java.math.BigDecimal;

public final class TestEvents {

    private TestEvents() {
    }

    public static UsageEvent storage(String userId, String resourceId, String quantity, long timestamp) {
        return event(userId, resourceId, ServiceType.STORAGE, UnitType.GB_HOUR, quantity, timestamp);
    }

    public static UsageEvent compute(String userId, String resourceId, String quantity, long timestamp) {
        return event(userId, resourceId, ServiceType.COMPUTE, UnitType.COMPUTE_HOUR, quantity, timestamp);
    }

    public static UsageEvent api(String userId, String resourceId, String quantity, long timestamp) {
        return event(userId, resourceId, ServiceType.API, UnitType.API_CALL, quantity, timestamp);
    }

    public static UsageEvent event(
            String userId,
            String resourceId,
            ServiceType serviceType,
            UnitType unit,
            String quantity,
            long timestamp) {
        return UsageEvent.of(userId, resourceId, serviceType, unit, new BigDecimal(quantity), timestamp);
    }
}
