package com.billing.entity.common;

import com.billing.enums.ServiceType;
import com.billing.enums.UnitType;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the business dimensions on which
 * usage is aggregated and billed.
 */
public final class UsageDimension
        implements Comparable<UsageDimension>, Serializable {

    private final UserId userId;

    private final ResourceId resourceId;

    private final ServiceType serviceType;

    private final UnitType unitType;

    private UsageDimension(
            UserId userId,
            ResourceId resourceId,
            ServiceType serviceType,
            UnitType unitType) {

        this.userId = Objects.requireNonNull(userId);
        this.resourceId = Objects.requireNonNull(resourceId);
        this.serviceType = Objects.requireNonNull(serviceType);
        this.unitType = Objects.requireNonNull(unitType);
    }

    public static UsageDimension of(
            UserId userId,
            ResourceId resourceId,
            ServiceType serviceType,
            UnitType unitType) {

        return new UsageDimension(
                userId,
                resourceId,
                serviceType,
                unitType
        );
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

    @Override
    public int compareTo(UsageDimension other) {

        int result = userId.compareTo(other.userId);

        if (result != 0) return result;

        result = resourceId.compareTo(other.resourceId);

        if (result != 0) return result;

        result = serviceType.compareTo(other.serviceType);

        if (result != 0) return result;

        return unitType.compareTo(other.unitType);
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof UsageDimension other)) {
            return false;
        }

        return Objects.equals(userId, other.userId)
                && Objects.equals(resourceId, other.resourceId)
                && serviceType == other.serviceType
                && unitType == other.unitType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                userId,
                resourceId,
                serviceType,
                unitType
        );
    }

    @Override
    public String toString() {
        return "UsageDimension{" +
                "userId=" + userId +
                ", resourceId=" + resourceId +
                ", serviceType=" + serviceType +
                ", unitType=" + unitType +
                '}';
    }
}