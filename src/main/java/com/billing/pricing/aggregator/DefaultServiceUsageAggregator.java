package com.billing.pricing.aggregator;

import com.billing.entity.common.ServiceUsageKey;
import com.billing.entity.common.UsageQuantity;
import com.billing.entity.usage.ResourceUsageSummary;
import com.billing.entity.usage.ServiceUsageSummary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DefaultServiceUsageAggregator
        implements ServiceUsageAggregator {

    @Override
    public List<ServiceUsageSummary> aggregate(
            List<ResourceUsageSummary> resourceUsageSummaries) {

        Map<ServiceUsageKey, Aggregate> groupedUsage =
                new LinkedHashMap<>();

        for (ResourceUsageSummary summary : resourceUsageSummaries) {

            ServiceUsageKey key =
                    ServiceUsageKey.of(
                            summary.getDimension().getUserId(),
                            summary.getDimension().getServiceType(),
                            summary.getDimension().getUnitType());

            groupedUsage
                    .computeIfAbsent(
                            key,
                            ignored -> new Aggregate())
                    .add(summary);
        }

        List<ServiceUsageSummary> result =
                new ArrayList<>();

        for (Map.Entry<ServiceUsageKey, Aggregate> entry
                : groupedUsage.entrySet()) {

            ServiceUsageKey key = entry.getKey();
            Aggregate aggregate = entry.getValue();

            result.add(

                    new ServiceUsageSummary(

                            key.getUserId(),

                            key.getServiceType(),

                            key.getUnitType(),

                            aggregate.getTotalQuantity(),

                            aggregate.getResourceSummaries()
                    )
            );
        }

        return List.copyOf(result);
    }

    /**
     * Internal accumulator used while grouping
     * ResourceUsageSummary objects into a
     * ServiceUsageSummary.
     */
    private static final class Aggregate {

        private UsageQuantity totalQuantity =
                UsageQuantity.ZERO;

        private final List<ResourceUsageSummary> resourceSummaries =
                new ArrayList<>();

        void add(ResourceUsageSummary summary) {

            totalQuantity =
                    totalQuantity.add(
                            summary.getTotalQuantity());

            resourceSummaries.add(summary);
        }

        UsageQuantity getTotalQuantity() {
            return totalQuantity;
        }

        List<ResourceUsageSummary> getResourceSummaries() {
            return List.copyOf(resourceSummaries);
        }
    }
}