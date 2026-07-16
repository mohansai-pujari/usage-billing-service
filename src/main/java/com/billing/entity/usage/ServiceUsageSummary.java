package com.billing.entity.usage;

import com.billing.entity.common.UsageQuantity;
import com.billing.entity.common.UserId;
import com.billing.enums.ServiceType;
import com.billing.enums.UnitType;

import java.util.List;
import java.util.Objects;

/**
 * Represents the total usage of a service
 * during a billing period.
 *
 * Example:
 *
 * STORAGE
 *      Disk-1 -> 120 GB
 *      Disk-2 -> 80 GB
 *
 * Total = 200 GB
 */
public final class ServiceUsageSummary {

    private final UserId userId;

    private final ServiceType serviceType;

    private final UnitType unitType;

    private final UsageQuantity totalQuantity;

    private final List<ResourceUsageSummary> resourceUsages;

    public ServiceUsageSummary(
            UserId userId,
            ServiceType serviceType,
            UnitType unitType,
            UsageQuantity totalQuantity,
            List<ResourceUsageSummary> resourceUsages) {

        this.userId = Objects.requireNonNull(userId);

        this.serviceType = Objects.requireNonNull(serviceType);

        this.unitType = Objects.requireNonNull(unitType);

        this.totalQuantity = Objects.requireNonNull(totalQuantity);

        this.resourceUsages = List.copyOf(resourceUsages);
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

    public UsageQuantity getTotalQuantity() {
        return totalQuantity;
    }

    public List<ResourceUsageSummary> getResourceUsages() {
        return resourceUsages;
    }
}