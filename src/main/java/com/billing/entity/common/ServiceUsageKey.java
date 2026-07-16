package com.billing.entity.common;

import com.billing.enums.ServiceType;
import com.billing.enums.UnitType;

import java.io.Serializable;
import java.util.Objects;

public final class ServiceUsageKey
        implements Comparable<ServiceUsageKey>, Serializable {

    private final UserId userId;

    private final ServiceType serviceType;

    private final UnitType unitType;

    private ServiceUsageKey(
            UserId userId,
            ServiceType serviceType,
            UnitType unitType) {

        this.userId = Objects.requireNonNull(userId);
        this.serviceType = Objects.requireNonNull(serviceType);
        this.unitType = Objects.requireNonNull(unitType);
    }

    public static ServiceUsageKey of(
            UserId userId,
            ServiceType serviceType,
            UnitType unitType) {

        return new ServiceUsageKey(
                userId,
                serviceType,
                unitType);
    }

    public UserId getUserId() {
        return userId;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    @Override
    public int compareTo(ServiceUsageKey other) {

        int result = userId.compareTo(other.userId);

        if (result != 0) {
            return result;
        }

        result = serviceType.compareTo(other.serviceType);

        if (result != 0) {
            return result;
        }

        return unitType.compareTo(other.unitType);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof ServiceUsageKey other)) {
            return false;
        }

        return Objects.equals(userId, other.userId)
                && serviceType == other.serviceType
                && unitType == other.unitType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                userId,
                serviceType,
                unitType);
    }
}