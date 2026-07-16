package com.billing.pricing.aggregator;

import com.billing.entity.common.UsageDimension;
import com.billing.entity.common.UsageQuantity;
import com.billing.entity.usage.UsageEvent;
import com.billing.entity.usage.ResourceUsageSummary;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DefaultUsageAggregator implements UsageAggregator {

    @Override
    public List<ResourceUsageSummary> aggregate(
            List<UsageEvent> usageEvents) {

        Map<UsageDimension, UsageQuantity> aggregated =
                new LinkedHashMap<>();

        for (UsageEvent event : usageEvents) {

            UsageDimension dimension =
                    UsageDimension.of(
                            event.getUserId(),
                            event.getResourceId(),
                            event.getServiceType(),
                            event.getUnitType());

            aggregated.merge(
                    dimension,
                    event.getQuantity(),
                    UsageQuantity::add);
        }

        return aggregated.entrySet()
                .stream()
                .map(entry ->
                        new ResourceUsageSummary(
                                entry.getKey(),
                                entry.getValue()))
                .toList();
    }
}