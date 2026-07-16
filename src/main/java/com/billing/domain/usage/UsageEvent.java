package com.billing.domain.usage;

import com.billing.domain.common.ServiceKey;
import com.billing.domain.common.UnitKey;
import com.billing.domain.common.UsageQuantity;
import com.billing.domain.common.ValidationRules;
import com.billing.exception.InvalidRequestException;

import java.time.Instant;

/** Immutable record of a single resource consumption event. */
public record UsageEvent(
        String userId,
        String resourceId,
        ServiceKey serviceType,
        UnitKey unit,
        UsageQuantity quantity,
        Instant timestamp
) {
    public UsageEvent {
        userId = normalizeId(userId, "User id");
        resourceId = normalizeId(resourceId, "Resource id");
        if (timestamp == null) {
            throw new InvalidRequestException("Timestamp is required.");
        }
    }

    private static String normalizeId(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidRequestException(field + " is required.");
        }
        String normalized = value.trim();
        if (normalized.length() > ValidationRules.MAX_ID_LENGTH) {
            throw new InvalidRequestException(field + " exceeds maximum length of " + ValidationRules.MAX_ID_LENGTH + ".");
        }
        return normalized;
    }
}
