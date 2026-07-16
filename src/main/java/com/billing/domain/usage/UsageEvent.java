package com.billing.domain.usage;

import com.billing.domain.common.UsageQuantity;
import com.billing.domain.common.ValidationRules;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;
import com.billing.exception.InvalidRequestException;

import java.math.BigDecimal;

public record UsageEvent(
        String userId,
        String resourceId,
        ServiceType serviceType,
        UnitType unit,
        UsageQuantity quantity,
        long timestamp,
        String eventId
) {
    public static UsageEvent of(
            String userId,
            String resourceId,
            ServiceType serviceType,
            UnitType unit,
            String quantity,
            long timestamp) {
        return of(userId, resourceId, serviceType, unit, UsageQuantity.of(quantity), timestamp, null);
    }

    public static UsageEvent of(
            String userId,
            String resourceId,
            ServiceType serviceType,
            UnitType unit,
            String quantity,
            long timestamp,
            String eventId) {
        return of(userId, resourceId, serviceType, unit, UsageQuantity.of(quantity), timestamp, eventId);
    }

    public static UsageEvent of(
            String userId,
            String resourceId,
            ServiceType serviceType,
            UnitType unit,
            BigDecimal quantity,
            long timestamp) {
        return of(userId, resourceId, serviceType, unit, quantity, timestamp, null);
    }

    public static UsageEvent of(
            String userId,
            String resourceId,
            ServiceType serviceType,
            UnitType unit,
            BigDecimal quantity,
            long timestamp,
            String eventId) {
        return of(userId, resourceId, serviceType, unit, UsageQuantity.of(quantity), timestamp, eventId);
    }

    public static UsageEvent of(
            String userId,
            String resourceId,
            ServiceType serviceType,
            UnitType unit,
            UsageQuantity quantity,
            long timestamp,
            String eventId) {
        return new UsageEvent(userId, resourceId, serviceType, unit, quantity, timestamp, eventId);
    }

    public UsageEvent {
        userId = normalizeId(userId, "User id");
        resourceId = normalizeId(resourceId, "Resource id");
        eventId = normalizeOptionalId(eventId, "Event id");
        if (unit == null) {
            throw new InvalidRequestException("Unit type is required.");
        }
        if (timestamp < 0) {
            throw new InvalidRequestException("Timestamp must be non-negative.");
        }
    }

    public boolean hasEventId() {
        return eventId != null;
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

    private static String normalizeOptionalId(String value, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > ValidationRules.MAX_ID_LENGTH) {
            throw new InvalidRequestException(field + " exceeds maximum length of " + ValidationRules.MAX_ID_LENGTH + ".");
        }
        return normalized;
    }
}
