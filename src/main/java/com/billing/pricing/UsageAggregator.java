package com.billing.pricing;

import com.billing.domain.common.UsageQuantity;
import com.billing.domain.usage.ResourceUsageSummary;
import com.billing.domain.usage.ServiceUsageSummary;
import com.billing.domain.usage.UsageEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Aggregates raw usage events into resource- and service-level summaries. Order-independent. */
public class UsageAggregator implements UsageAggregationStrategy {

    @Override
    public List<ServiceUsageSummary> aggregate(List<UsageEvent> events) {
        return groupByService(groupByResource(events));
    }

    private List<ResourceUsageSummary> groupByResource(List<UsageEvent> events) {
        Map<String, ResourceUsageSummary> grouped = new LinkedHashMap<>();
        for (UsageEvent event : events) {
            String key = event.userId() + "|" + event.resourceId() + "|" + event.serviceType().value() + "|" + event.unit().value();
            grouped.merge(
                    key,
                    new ResourceUsageSummary(
                            event.userId(),
                            event.resourceId(),
                            event.serviceType(),
                            event.unit(),
                            event.quantity()),
                    (existing, incoming) -> new ResourceUsageSummary(
                            existing.userId(),
                            existing.resourceId(),
                            existing.serviceType(),
                            existing.unit(),
                            existing.quantity().add(incoming.quantity())));
        }
        return List.copyOf(grouped.values());
    }

    private List<ServiceUsageSummary> groupByService(List<ResourceUsageSummary> resourceUsages) {
        Map<String, ServiceUsageBuilder> grouped = new LinkedHashMap<>();
        for (ResourceUsageSummary usage : resourceUsages) {
            String key = usage.userId() + "|" + usage.serviceType().value() + "|" + usage.unit().value();
            grouped.computeIfAbsent(
                            key,
                            ignored -> new ServiceUsageBuilder(usage.userId(), usage.serviceType(), usage.unit()))
                    .add(usage);
        }
        return grouped.values().stream().map(ServiceUsageBuilder::build).toList();
    }

    private static final class ServiceUsageBuilder {
        private final String userId;
        private final com.billing.domain.common.ServiceKey serviceType;
        private final com.billing.domain.common.UnitKey unit;
        private UsageQuantity totalQuantity;
        private final List<ResourceUsageSummary> resources = new ArrayList<>();

        private ServiceUsageBuilder(
                String userId,
                com.billing.domain.common.ServiceKey serviceType,
                com.billing.domain.common.UnitKey unit) {
            this.userId = userId;
            this.serviceType = serviceType;
            this.unit = unit;
        }

        private void add(ResourceUsageSummary usage) {
            totalQuantity = totalQuantity == null ? usage.quantity() : totalQuantity.add(usage.quantity());
            resources.add(usage);
        }

        private ServiceUsageSummary build() {
            return new ServiceUsageSummary(userId, serviceType, unit, totalQuantity, List.copyOf(resources));
        }
    }
}
