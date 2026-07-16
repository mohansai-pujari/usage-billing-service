package com.billing.entity.usage;

import com.billing.entity.common.ResourceId;
import com.billing.entity.common.UsageQuantity;
import com.billing.entity.common.UserId;
import com.billing.enums.ServiceType;
import com.billing.enums.UnitType;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable domain object representing a single usage event.
 *
 * A usage event records the consumption of a particular service
 * by a user on a specific resource at a given point in time.
 */
public final class UsageEvent {

    private final UserId userId;

    private final ResourceId resourceId;

    private final ServiceType serviceType;

    private final UnitType unitType;

    private final UsageQuantity quantity;

    private final Instant timestamp;

    public UsageEvent(
            UserId userId,
            ResourceId resourceId,
            ServiceType serviceType,
            UnitType unitType,
            UsageQuantity quantity,
            Instant timestamp) {

        this.userId = Objects.requireNonNull(userId, "UserId cannot be null.");
        this.resourceId = Objects.requireNonNull(resourceId, "ResourceId cannot be null.");
        this.serviceType = Objects.requireNonNull(serviceType, "ServiceType cannot be null.");
        this.unitType = Objects.requireNonNull(unitType, "UnitType cannot be null.");
        this.quantity = Objects.requireNonNull(quantity, "Usage quantity cannot be null.");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null.");
    }

    public UserId getUserId() {
        return userId;
    }

    public ResourceId getResourceId() {
        return resourceId;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public UsageQuantity getQuantity() {
        return quantity;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof UsageEvent other)) {
            return false;
        }

        return Objects.equals(userId, other.userId)
                && Objects.equals(resourceId, other.resourceId)
                && serviceType == other.serviceType
                && unitType == other.unitType
                && Objects.equals(quantity, other.quantity)
                && Objects.equals(timestamp, other.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                userId,
                resourceId,
                serviceType,
                unitType,
                quantity,
                timestamp
        );
    }

    @Override
    public String toString() {
        return "UsageEvent{" +
                "userId=" + userId +
                ", resourceId=" + resourceId +
                ", serviceType=" + serviceType +
                ", unitType=" + unitType +
                ", quantity=" + quantity +
                ", timestamp=" + timestamp +
                '}';
    }
}