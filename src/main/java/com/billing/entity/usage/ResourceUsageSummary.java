package com.billing.entity.usage;

import com.billing.entity.common.UsageDimension;
import com.billing.entity.common.UsageQuantity;

import java.util.Objects;

/**
 * Represents the aggregated usage of a single
 * user-resource-service combination within
 * a billing period.
 */

public final class ResourceUsageSummary {

    private final UsageDimension dimension;

    private final UsageQuantity totalQuantity;

    public ResourceUsageSummary(UsageDimension dimension, UsageQuantity totalQuantity) {

        this.dimension = dimension;
        this.totalQuantity = Objects.requireNonNull(totalQuantity);
    }

    public UsageDimension getDimension() {
        return dimension;
    }

    public UsageQuantity getTotalQuantity() {
        return totalQuantity;
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof ResourceUsageSummary other)) {
            return false;
        }

        return Objects.equals(dimension, other.dimension)
                && Objects.equals(totalQuantity, other.totalQuantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                dimension,
                totalQuantity
        );
    }

    @Override
    public String toString() {
        return "UsageSummary{" +
                "dimension=" + dimension +
                ", totalQuantity=" + totalQuantity +
                '}';
    }
}